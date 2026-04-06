package com.example.bidoo_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuctionItemResponse {

    private Long id;
    private String title;
    private String description;
}