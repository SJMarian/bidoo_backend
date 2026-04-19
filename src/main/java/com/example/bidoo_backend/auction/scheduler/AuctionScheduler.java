package com.example.bidoo_backend.auction.scheduler;

import com.example.bidoo_backend.auction.service.AuctionLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks that drive the auction lifecycle automatically.
 *
 * Every 10 seconds: close any ACTIVE auctions whose end time has passed.
 * Every 30 seconds: activate any UPCOMING auctions whose start time has passed.
 *
 * This means an auction will close within 10 seconds of its end time —
 * accurate enough for an auction system without requiring a per-auction timer.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final AuctionLifecycleService lifecycleService;

    /**
     * Check every 10 seconds for auctions that need to be closed.
     * fixedDelay means it waits 10s AFTER the previous run finishes,
     * preventing overlapping executions.
     */
    @Scheduled(fixedDelay = 10_000)
    public void closeExpiredAuctions() {
        log.debug("Scheduler: checking for expired auctions...");
        lifecycleService.closeExpiredAuctions();
    }

    /**
     * Check every 30 seconds for upcoming auctions that should now be active.
     */
    @Scheduled(fixedDelay = 30_000)
    public void activateDueAuctions() {
        log.debug("Scheduler: checking for auctions to activate...");
        lifecycleService.activateDueAuctions();
    }
}
