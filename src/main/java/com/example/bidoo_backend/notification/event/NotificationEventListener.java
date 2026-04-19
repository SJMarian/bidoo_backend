package com.example.bidoo_backend.notification.event;

import com.example.bidoo_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens to auction lifecycle events and triggers the appropriate notifications.
 * Runs asynchronously so it never blocks the main auction/bid transaction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * When an auction closes, notify the winner to complete payment.
     */
    @Async
    @EventListener
    public void handleAuctionClosed(AuctionClosedEvent event) {
        log.info("Handling AuctionClosedEvent for auction {} (winner: {})",
                event.getAuctionId(), event.getWinnerId());

        if (event.getWinnerId() != null) {
            notificationService.notifyAuctionWinner(
                    event.getWinnerId(),
                    event.getAuctionId(),
                    event.getAuctionTitle()
            );
        }
    }

    /**
     * When a bid is placed:
     *   1. Confirm to the bidder their bid was accepted.
     *   2. Notify the previous highest bidder they were outbid.
     */
    @Async
    @EventListener
    public void handleBidPlaced(BidPlacedEvent event) {
        log.info("Handling BidPlacedEvent for auction {} by user {}",
                event.getAuctionId(), event.getNewBidderId());

        // Confirm bid to the current bidder
        notificationService.notifyBidPlaced(
                event.getNewBidderId(),
                event.getAuctionId(),
                event.getAuctionTitle(),
                event.getNewBidAmount()
        );

        // Notify the previously outbid user (if there was one)
        if (event.getPreviousHighestBidderId() != null
                && !event.getPreviousHighestBidderId().equals(event.getNewBidderId())) {
            notificationService.notifyOutbid(
                    event.getPreviousHighestBidderId(),
                    event.getAuctionId(),
                    event.getAuctionTitle(),
                    event.getNewBidAmount()
            );
        }
    }
}
