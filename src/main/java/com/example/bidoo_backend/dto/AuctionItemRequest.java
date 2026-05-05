package com.example.bidoo_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a new auction item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionItemRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String currency;

    @NotNull
    private Double bidStartingPrice;

    @NotNull
    private Double minimumBidIncrement;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Integer extendSeconds;
}
