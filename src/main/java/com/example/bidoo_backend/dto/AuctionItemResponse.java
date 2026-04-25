package com.example.bidoo_backend.dto;

import com.example.bidoo_backend.enums.AuctionItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO for created auction item.
 */
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
    private AuctionItemStatus status;
    private Long timeLeft;
    private Double minimumBidIncrement;
    private String currency;
}
