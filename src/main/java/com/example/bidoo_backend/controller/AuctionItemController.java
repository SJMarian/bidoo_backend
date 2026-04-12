package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.AuctionItemRequest;
import com.example.bidoo_backend.dto.AuctionItemResponse;
import com.example.bidoo_backend.dto.ApiResponse;
import com.example.bidoo_backend.entity.AuctionImage;
import com.example.bidoo_backend.entity.AuctionItem;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.enums.AuctionItemStatus;
import com.example.bidoo_backend.repository.AuctionImageRepository;
import com.example.bidoo_backend.repository.AuctionItemRepository;
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
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auction")
@RequiredArgsConstructor
@Validated
public class AuctionItemController {

    private final AuctionItemRepository auctionItemRepository;
    private final AuctionImageRepository auctionImageRepository;
    private final UserRepository userRepository;

    private static final String UPLOAD_DIR = "upload";

    @PostMapping(value = "/item-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AuctionItemResponse>> createAuctionItem(
            @Valid @RequestPart("request") AuctionItemRequest request,
            @RequestPart("images") List<MultipartFile> images,
            Principal principal) {

        if (images == null || images.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("At least 1 image is required",
                            HttpStatus.BAD_REQUEST.value()));
        }

        if (images.size() > 5) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("A maximum of 5 images is allowed",
                            HttpStatus.BAD_REQUEST.value()));
        }

        User seller = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        AuctionItem auctionItem = AuctionItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .seller(seller)
                .currency(request.getCurrency())
                .bidStartingPrice(request.getBidStartingPrice())
                .minimumBidIncrement(request.getMinimumBidIncrement())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .extendSeconds(request.getExtendSeconds())
                .status(AuctionItemStatus.PENDING)
                .totalBids(0)
                .currentHighestBid(0.0)
                .build();

        AuctionItem savedItem = auctionItemRepository.save(auctionItem);

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            List<AuctionImage> auctionImages = new ArrayList<>();

            for (MultipartFile image : images) {
                String originalFilename = image.getOriginalFilename();
                String extension = "";

                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String filename = UUID.randomUUID() + extension;
                Path filePath = uploadPath.resolve(filename);

                Files.copy(image.getInputStream(), filePath);

                AuctionImage auctionImage = AuctionImage.builder()
                        .auctionItem(savedItem)
                        .imageUrl(UPLOAD_DIR + "/" + filename)
                        .build();

                auctionImages.add(auctionImage);
            }

            auctionImageRepository.saveAll(auctionImages);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload images: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }

        AuctionItemResponse response = AuctionItemResponse.builder()
                .id(savedItem.getId())
                .title(savedItem.getTitle())
                .description(savedItem.getDescription())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(response, "Auction item created", HttpStatus.OK.value()));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<AuctionItemResponse>>> getAuctionItems(Principal principal) {

        User seller = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        final List<AuctionItem> items = auctionItemRepository.getBySeller(seller);

        List<AuctionItemResponse> responseList = items.stream()
                .map(item -> AuctionItemResponse.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .build())
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(responseList, "Auction items fetched", HttpStatus.OK.value()));
    }

    // SEARCH + FILTER
    @GetMapping("/search")
    public ResponseEntity<?> searchAuctions(
            @RequestParam(required = false) AuctionItemStatus status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        return ResponseEntity.ok(
                auctionItemRepository.searchAuctions(status, minPrice, maxPrice));
    }
}