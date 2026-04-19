package com.example.bidoo_backend.auction.dto;

import lombok.Data;

@Data
public class BidRequest {

    private Long auctionId;
    private Double amount;
}