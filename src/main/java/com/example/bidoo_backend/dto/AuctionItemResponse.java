package com.example.bidoo_backend.dto;

import com.example.bidoo_backend.enums.AuctionItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionItemResponse {
    private Long id;
    private String title;
    private String description;
    private String image;
    private Double currentHighestBid;
    private Double bidStartingPrice;
    private AuctionItemStatus status;
    private Long timeLeft;           // seconds until end
    private Double minimumBidIncrement;
    private String currency;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String sellerName;
    private Integer totalBids;
}
