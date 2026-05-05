package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.AuctionItemRequest;
import com.example.bidoo_backend.dto.AuctionItemResponse;
import com.example.bidoo_backend.dto.ApiResponse;
import com.example.bidoo_backend.entity.AuctionImage;
import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.repository.AuctionImageRepository;
import com.example.bidoo_backend.repository.AuctionRepository;
import com.example.bidoo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auction")
@RequiredArgsConstructor
@Validated
public class AuctionItemController {

    private final AuctionRepository auctionRepository;
    private final AuctionImageRepository auctionImageRepository;
    private final UserRepository userRepository;

    private static final String UPLOAD_DIR = "upload";

    // ── Helper: map Auction -> AuctionItemResponse ─────────────────────────
    private AuctionItemResponse toResponse(Auction item) {
        // Find first image for this auction
        String imageUrl = auctionImageRepository.findFirstByAuction(item)
                .map(AuctionImage::getImageUrl)
                .orElse(null);

        // Time left in seconds
        long timeLeftSeconds = 0;
        if (item.getEndAt() != null) {
            long diff = java.time.Duration.between(LocalDateTime.now(), item.getEndAt()).toSeconds();
            timeLeftSeconds = Math.max(0, diff);
        }

        return AuctionItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .image(imageUrl)
                .currentHighestBid(item.getCurrentHighestBid())
                .bidStartingPrice(item.getBidStartingPrice())
                .status(item.getStatus() != null
                        ? com.example.bidoo_backend.enums.AuctionItemStatus.valueOf(item.getStatus().name())
                        : null)
                .timeLeft(timeLeftSeconds)
                .minimumBidIncrement(item.getMinimumBidIncrement())
                .currency(item.getCurrency())
                .startAt(item.getStartAt())
                .endAt(item.getEndAt())
                .sellerName(item.getSeller() != null ? item.getSeller().getName() : null)
                .totalBids(item.getTotalBids())
                .build();
    }

    // ── POST /api/v1/auction/item-upload ───────────────────────────────────
    @PostMapping(value = "/item-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AuctionItemResponse>> createAuctionItem(
            @Valid @RequestPart("request") AuctionItemRequest request,
            @RequestPart("images") List<MultipartFile> images,
            Principal principal) {

        if (images == null || images.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("At least 1 image is required", HttpStatus.BAD_REQUEST.value()));
        }
        if (images.size() > 5) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("A maximum of 5 images is allowed", HttpStatus.BAD_REQUEST.value()));
        }

        User seller = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        Auction auction = Auction.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .seller(seller)
                .currency(request.getCurrency())
                .bidStartingPrice(request.getBidStartingPrice())
                .minimumBidIncrement(request.getMinimumBidIncrement())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .extendSeconds(request.getExtendSeconds())
                .status(AuctionStatus.PENDING)
                .totalBids(0)
                .currentHighestBid(0.0)
                .build();

        Auction savedItem = auctionRepository.save(auction);

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            List<AuctionImage> auctionImages = new ArrayList<>();
            for (MultipartFile image : images) {
                String orig = image.getOriginalFilename();
                String ext = (orig != null && orig.contains("."))
                        ? orig.substring(orig.lastIndexOf(".")) : "";
                String filename = UUID.randomUUID() + ext;
                Files.copy(image.getInputStream(), uploadPath.resolve(filename));
                auctionImages.add(AuctionImage.builder()
                        .auction(savedItem)
                        .imageUrl(UPLOAD_DIR + "/" + filename)
                        .build());
            }
            auctionImageRepository.saveAll(auctionImages);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload images: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }

        return ResponseEntity.ok(ApiResponse.success(toResponse(savedItem), "Auction item created", HttpStatus.OK.value()));
    }

    // ── GET /api/v1/auction/items — seller's own auctions ─────────────────
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<AuctionItemResponse>>> getMyAuctions(Principal principal) {
        User seller = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        List<AuctionItemResponse> result = auctionRepository.findBySeller(seller)
                .stream().map(this::toResponse).toList();

        return ResponseEntity.ok(ApiResponse.success(result, "Auction items fetched", HttpStatus.OK.value()));
    }

    // ── GET /api/v1/auction/items-mine — alias for items ──────────────────
    @GetMapping("/items-mine")
    public ResponseEntity<ApiResponse<List<AuctionItemResponse>>> getItemsMine(Principal principal) {
        return getMyAuctions(principal);
    }

    // ── GET /api/v1/auction/items-others — all ACTIVE/UPCOMING auctions ───
    @GetMapping("/items-others")
    public ResponseEntity<ApiResponse<List<AuctionItemResponse>>> getOthersAuctions(Principal principal) {
        User me = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<AuctionItemResponse> result = auctionRepository
                .findByStatusIn(List.of(AuctionStatus.ACTIVE, AuctionStatus.UPCOMING))
                .stream()
                .filter(a -> a.getSeller() == null || !a.getSeller().getId().equals(me.getId()))
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(result, "Auctions fetched", HttpStatus.OK.value()));
    }

    // ── GET /api/v1/auction/search — search/filter auctions ───────────────
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AuctionItemResponse>>> searchAuctions(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String currency) {

        List<Auction> all = auctionRepository.findAll();

        List<AuctionItemResponse> result = all.stream()
                .filter(a -> {
                    // Status filter
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
                        try {
                            if (!a.getStatus().name().equalsIgnoreCase(status)) return false;
                        } catch (Exception e) { return false; }
                    }
                    // Title filter
                    if (title != null && !title.isBlank()) {
                        if (a.getTitle() == null || !a.getTitle().toLowerCase().contains(title.toLowerCase()))
                            return false;
                    }
                    // Price filter
                    if (minPrice != null && a.getBidStartingPrice() != null && a.getBidStartingPrice() < minPrice)
                        return false;
                    if (maxPrice != null && a.getBidStartingPrice() != null && a.getBidStartingPrice() > maxPrice)
                        return false;
                    // Currency filter
                    if (currency != null && !currency.isBlank()) {
                        if (a.getCurrency() == null || !a.getCurrency().equalsIgnoreCase(currency))
                            return false;
                    }
                    return true;
                })
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(result, "Search results", HttpStatus.OK.value()));
    }
}
