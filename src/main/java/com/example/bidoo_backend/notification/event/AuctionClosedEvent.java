package com.example.bidoo_backend.notification.event;

import lombok.*;

/**
 * Application event fired when an auction is closed and a winner is determined.
 * Other modules (auction service) publish this event; the notification listener consumes it.
 *
 * Usage in auction service:
 *   applicationEventPublisher.publishEvent(
 *       new AuctionClosedEvent(this, auctionId, auctionTitle, winnerId, winningBid)
 *   );
 */
@Getter
public class AuctionClosedEvent extends org.springframework.context.ApplicationEvent {

    private final Long auctionId;
    private final String auctionTitle;
    private final Long winnerId;        // null if no bids were placed
    private final double winningBid;

    public AuctionClosedEvent(Object source, Long auctionId, String auctionTitle,
                              Long winnerId, double winningBid) {
        super(source);
        this.auctionId = auctionId;
        this.auctionTitle = auctionTitle;
        this.winnerId = winnerId;
        this.winningBid = winningBid;
    }
}
