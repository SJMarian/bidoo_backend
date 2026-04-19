package com.example.bidoo_backend.notification.event;

import lombok.*;

/**
 * Application event fired when a valid bid is placed.
 * The notification listener uses this to:
 *   1. Confirm the bid to the bidder
 *   2. Notify the previous highest bidder that they were outbid
 *
 * Usage in bid service:
 *   applicationEventPublisher.publishEvent(
 *       new BidPlacedEvent(this, auctionId, auctionTitle, newBidderId, newBidAmount,
 *                          previousHighestBidderId, previousBidAmount)
 *   );
 */
@Getter
public class BidPlacedEvent extends org.springframework.context.ApplicationEvent {

    private final Long auctionId;
    private final String auctionTitle;
    private final Long newBidderId;
    private final double newBidAmount;
    private final Long previousHighestBidderId; // null if this is the first bid
    private final double previousBidAmount;

    public BidPlacedEvent(Object source, Long auctionId, String auctionTitle,
                          Long newBidderId, double newBidAmount,
                          Long previousHighestBidderId, double previousBidAmount) {
        super(source);
        this.auctionId = auctionId;
        this.auctionTitle = auctionTitle;
        this.newBidderId = newBidderId;
        this.newBidAmount = newBidAmount;
        this.previousHighestBidderId = previousHighestBidderId;
        this.previousBidAmount = previousBidAmount;
    }
}
