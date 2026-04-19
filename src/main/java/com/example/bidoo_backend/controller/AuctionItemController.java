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
import java.time.LocalDateTime;
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
                    .body(ApiResponse.error("At least 1 image is required", 400));
        }

        User seller = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        LocalDateTime now = LocalDateTime.now();
        AuctionItemStatus autoStatus;

        if (now.isBefore(request.getStartAt())) {
            autoStatus = AuctionItemStatus.PENDING;
        } else if (now.isAfter(request.getEndAt())) {
            autoStatus = AuctionItemStatus.ENDED;
        } else {
            autoStatus = AuctionItemStatus.LIVE;
        }

        AuctionItem item = AuctionItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .seller(seller)
                .currency(request.getCurrency())
                .bidStartingPrice(request.getBidStartingPrice())
                .minimumBidIncrement(request.getMinimumBidIncrement())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .extendSeconds(request.getExtendSeconds())
                .status(autoStatus)
                .totalBids(0)
                .currentHighestBid(0.0)
                .build();

        AuctionItem saved = auctionItemRepository.save(item);

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            List<AuctionImage> imagesToSave = new ArrayList<>();

            for (MultipartFile file : images) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path path = uploadPath.resolve(fileName);

                Files.copy(file.getInputStream(), path);

                imagesToSave.add(
                        AuctionImage.builder()
                                .auctionItem(saved)
                                .imageUrl("upload/" + fileName)
                                .build()
                );
            }

            auctionImageRepository.saveAll(imagesToSave);

        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Image upload failed", 500));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        AuctionItemResponse.builder()
                                .id(saved.getId())
                                .title(saved.getTitle())
                                .description(saved.getDescription())
                                .build(),
                        "Auction created",
                        HttpStatus.OK.value()
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAuctions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AuctionItemStatus status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean endingSoon,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer minBids,
            @RequestParam(required = false) Integer maxBids) {

        boolean noFilters =
                keyword == null &&
                status == null &&
                minPrice == null &&
                maxPrice == null &&
                endingSoon == null &&
                startDate == null &&
                endDate == null &&
                minBids == null &&
                maxBids == null;

        if (noFilters) {
            return ResponseEntity.ok(auctionItemRepository.findAll());
        }

        LocalDateTime soonTime = Boolean.TRUE.equals(endingSoon)
                ? LocalDateTime.now().plusMinutes(2)
                : LocalDateTime.now().plusYears(100);

        return ResponseEntity.ok(
                auctionItemRepository.searchAuctions(
                        keyword,
                        status != null ? status.name() : null,
                        minPrice,
                        maxPrice,
                        endingSoon,
                        startDate,
                        endDate,
                        minBids,
                        maxBids,
                        soonTime
                )
        );
    }
}