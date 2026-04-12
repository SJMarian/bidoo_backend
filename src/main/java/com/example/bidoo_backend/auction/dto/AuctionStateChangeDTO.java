package com.example.bidoo_backend.auction.dto;

import com.example.bidoo_backend.auction.entity.AuctionState;
import lombok.*;

/**
 * Broadcast to all clients watching an auction whenever its state changes.
 * Clients use this to:
 *   - Stop accepting bid input when state = CLOSED
 *   - Show the "Auction Closed" animation
 *   - Update UI state without a page refresh
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionStateChangeDTO {
    private Long auctionId;
    private AuctionState previousState;
    private AuctionState newState;
    private Long winnerId;          // null if no bids were placed
    private double winningBid;
    private long serverTimeMs;      // server time of the state change
}
