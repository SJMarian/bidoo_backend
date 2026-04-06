package com.example.bidoo_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuctionItemRequest {

    private String title;
    private String description;
    private String currency;

    private Double bidStartingPrice;
    private Double minimumBidIncrement;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private int extendSeconds;

    private List<String> imageUrls;
}