package com.example.bidoo_backend.dto;

import com.example.bidoo_backend.enums.AuctionItemStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuctionSearchRequest {
    private AuctionItemStatus status;
    private Double minPrice;
    private Double maxPrice;
    private Boolean endingSoon;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minBids;
}