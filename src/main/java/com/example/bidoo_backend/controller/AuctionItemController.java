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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auction")
@RequiredArgsConstructor
@Validated
public class AuctionItemController {

    private final AuctionItemRepository auctionItemRepository;
    private final AuctionImageRepository auctionImageRepository;
    private final UserRepository userRepository;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<AuctionItemResponse>> createAuctionItem(
            @Valid @RequestBody AuctionItemRequest request, Principal principal) {

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

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<AuctionImage> images = request.getImageUrls().stream()
                    .map(url -> AuctionImage.builder()
                            .auctionItem(savedItem)
                            .imageUrl(url)
                            .build())
                    .collect(Collectors.toList());
            auctionImageRepository.saveAll(images);
        }

        AuctionItemResponse response = AuctionItemResponse.builder()
                .id(savedItem.getId())
                .title(savedItem.getTitle())
                .description(savedItem.getDescription())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Auction item created", HttpStatus.OK.value()));
    }
}
