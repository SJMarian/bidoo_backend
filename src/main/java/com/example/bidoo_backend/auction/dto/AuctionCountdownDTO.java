package com.example.bidoo_backend.auction.dto;

import com.example.bidoo_backend.auction.entity.AuctionState;
import lombok.*;

/**
 * Contains all timing data the frontend needs to render an accurate countdown.
 * The frontend calculates: remainingMs = endTimeMs - serverTimeMs + (Date.now() - fetchedAt)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionCountdownDTO {
    private Long auctionId;
    private String auctionTitle;
    private AuctionState state;

    /** Auction end time in epoch milliseconds (server clock) */
    private long endTimeMs;

    /** Auction start time in epoch milliseconds (server clock) */
    private long startTimeMs;

    /** Current server time in epoch milliseconds when this DTO was built */
    private long serverTimeMs;

    /** Remaining milliseconds until end (or 0 if already closed) */
    private long remainingMs;
}
