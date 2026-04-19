package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.AuctionItemRequest;
import com.example.bidoo_backend.dto.AuctionItemResponse;
import com.example.bidoo_backend.dto.ApiResponse;
import com.example.bidoo_backend.entity.AuctionImage;
import com.example.bidoo_backend.entity.AuctionItem;
import com.example.bidoo_backend.entity.User;
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
import java.time.Duration;
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
                                .status(com.example.bidoo_backend.enums.AuctionItemStatus.PENDING)
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

                return ResponseEntity.ok(ApiResponse.success(response, "Auction item created", HttpStatus.OK.value()));
        }


        @GetMapping("/items-mine")
        public ResponseEntity<ApiResponse<List<AuctionItemResponse>>> getMyAuctionItems(Principal principal) {
           
            User seller = userRepository.findByEmail(principal.getName())
                                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

            final List<AuctionItem> items = auctionItemRepository.getBySeller(seller);

            List<AuctionItemResponse> responseList = items.stream()
                    .map(item -> {
                        List<AuctionImage> images = auctionImageRepository.findByAuctionItem(item);
                        String imageUrl = !images.isEmpty() ? images.get(0).getImageUrl() : null;
                        
                        if (imageUrl != null && !imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                            imageUrl = "http://localhost:8080/" + imageUrl;
                        }

                        Long timeLeft = null;
                        LocalDateTime now = LocalDateTime.now();
                        if (item.getStatus() == com.example.bidoo_backend.enums.AuctionItemStatus.PENDING || 
                            item.getStatus() == com.example.bidoo_backend.enums.AuctionItemStatus.SCHEDULED) {
                            if (item.getStartAt() != null) {
                                timeLeft = Duration.between(now, item.getStartAt()).toMillis();
                                if (timeLeft < 0) timeLeft = 0L;
                            }
                        } else if (item.getStatus() == com.example.bidoo_backend.enums.AuctionItemStatus.LIVE) {
                            if (item.getEndAt() != null) {
                                timeLeft = Duration.between(now, item.getEndAt()).toMillis();
                                if (timeLeft < 0) timeLeft = 0L;
                            }
                        }

                        return AuctionItemResponse.builder()
                                .id(item.getId())
                                .title(item.getTitle())
                                .description(item.getDescription())
                                .image(imageUrl)
                                .currentHighestBid(item.getCurrentHighestBid())
                                .status(item.getStatus())
                                .timeLeft(timeLeft)
                                .build();
                    })
                    .toList();

            return ResponseEntity.ok(
                    ApiResponse.success(responseList, "Auction items fetched", HttpStatus.OK.value())
            );
        }
        
}
