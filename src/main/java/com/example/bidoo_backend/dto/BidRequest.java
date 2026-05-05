package com.example.bidoo_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequest {

    @NotNull
    private Long auctionItemId;

    @NotNull
    private Double bidAmount;
}