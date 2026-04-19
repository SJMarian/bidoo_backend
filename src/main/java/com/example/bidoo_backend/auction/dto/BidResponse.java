package com.example.bidoo_backend.auction.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BidResponse {
    private Long bidId;
    private Double amount;
    private String message;
}