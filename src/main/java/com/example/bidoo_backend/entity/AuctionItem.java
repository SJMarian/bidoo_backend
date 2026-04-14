package com.example.bidoo_backend.entity;

import lombok.*;
import java.time.LocalDateTime;
import com.example.bidoo_backend.enums.AuctionItemStatus;

// DEPRECATED: Consolidated into Auction.java entity
// Keeping as reference only - DO NOT USE
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionItem {

    // DEPRECATED fields - see Auction.java for live implementation
    private Long id;

    private String title;

    private String description;

    private User seller;

    private String currency;

    private Double bidStartingPrice;
    private Double minimumBidIncrement;

    private Double currentHighestBid;

    private User currentHighestBidder;

    private Integer totalBids;

    private AuctionItemStatus status;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Integer extendSeconds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}