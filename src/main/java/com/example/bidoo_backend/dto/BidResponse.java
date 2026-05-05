package com.example.bidoo_backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    private Long bidId;
    private Long auctionItemId;
    private Double bidAmount;
    private String bidderEmail;
    private Double currentHighestBid;
    private Integer totalBids;
    private Long version;
    private LocalDateTime bidTime;
}
