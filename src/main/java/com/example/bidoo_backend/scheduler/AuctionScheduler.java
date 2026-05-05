package com.example.bidoo_backend.scheduler;

import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.repository.AuctionRepository;
import com.example.bidoo_backend.repository.BidRepository;
import com.example.bidoo_backend.repository.UserRepository;
import com.example.bidoo_backend.service.AuctionNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks that automatically:
 *  1. Activate UPCOMING auctions when their start time arrives
 *  2. Close ACTIVE auctions when their end time passes + notify winner & losers
 *  3. Send "ending soon" alerts 1 hour before an auction closes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final AuctionNotificationService auctionNotificationService;

    /**
     * Every minute: activate auctions whose start time has arrived.
     * UPCOMING → ACTIVE
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void activateUpcomingAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> toActivate = auctionRepository.findByStatus(AuctionStatus.UPCOMING)
                .stream()
                .filter(a -> a.getStartAt() != null && !a.getStartAt().isAfter(now))
                .toList();

        for (Auction auction : toActivate) {
            auction.setStatus(AuctionStatus.ACTIVE);
            auctionRepository.save(auction);
            log.info("Auction {} '{}' is now ACTIVE", auction.getId(), auction.getTitle());
        }
    }

    /**
     * Every minute: close auctions whose end time has passed.
     * ACTIVE → CLOSED, then notify winner and losers.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> toClose = auctionRepository.findByStatus(AuctionStatus.ACTIVE)
                .stream()
                .filter(a -> a.getEndAt() != null && a.getEndAt().isBefore(now))
                .toList();

        for (Auction auction : toClose) {
            auction.setStatus(AuctionStatus.CLOSED);
            auctionRepository.save(auction);
            log.info("Auction {} '{}' closed", auction.getId(), auction.getTitle());
            notifyAuctionClosed(auction);
        }
    }

    /**
     * Every 5 minutes: send "ending soon" notifications for auctions
     * ending within the next hour that haven't been notified yet.
     */
    @Scheduled(fixedDelay = 300_000)
    public void sendEndingSoonNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);

        List<Auction> endingSoon = auctionRepository.findByStatus(AuctionStatus.ACTIVE)
                .stream()
                .filter(a -> a.getEndAt() != null
                        && a.getEndAt().isAfter(now)
                        && a.getEndAt().isBefore(oneHourFromNow))
                .toList();

        for (Auction auction : endingSoon) {
            notifyAllBidders(auction, (userId) ->
                auctionNotificationService.notifyAuctionEndingSoon(
                    userId, auction.getTitle(), auction.getId())
            );
            // Also notify seller
            if (auction.getSeller() != null) {
                try {
                    auctionNotificationService.notifyAuctionEndingSoon(
                        auction.getSeller().getId(), auction.getTitle(), auction.getId());
                } catch (Exception e) {
                    log.warn("Failed to notify seller of ending soon: {}", e.getMessage());
                }
            }
            log.info("Ending-soon notifications sent for auction {}", auction.getId());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void notifyAuctionClosed(Auction auction) {
        Long winnerId = auction.getCurrentHighestBidder() != null
                ? auction.getCurrentHighestBidder().getId() : null;

        // Notify winner
        if (winnerId != null) {
            try {
                auctionNotificationService.notifyAuctionWinner(
                    winnerId, auction.getTitle(), auction.getCurrentHighestBid(), auction.getId());
                auctionNotificationService.notifyPaymentRequired(
                    winnerId, auction.getCurrentHighestBid(), auction.getTitle(), auction.getId());
            } catch (Exception e) {
                log.warn("Failed to notify winner {}: {}", winnerId, e.getMessage());
            }
        }

        // Notify losing bidders
        bidRepository.findByAuctionIdOrderByAmountDesc(auction.getId())
            .stream()
            .map(bid -> userRepository.findByEmail(bid.getBidderUsername()))
            .filter(opt -> opt.isPresent())
            .map(opt -> opt.get().getId())
            .distinct()
            .filter(userId -> !userId.equals(winnerId))
            .forEach(userId -> {
                try {
                    auctionNotificationService.notifyAuctionLost(
                        userId, auction.getTitle(), auction.getId());
                } catch (Exception e) {
                    log.warn("Failed to notify losing bidder {}: {}", userId, e.getMessage());
                }
            });
    }

    private void notifyAllBidders(Auction auction, java.util.function.Consumer<Long> notifyFn) {
        bidRepository.findByAuctionIdOrderByAmountDesc(auction.getId())
            .stream()
            .map(bid -> userRepository.findByEmail(bid.getBidderUsername()))
            .filter(opt -> opt.isPresent())
            .map(opt -> opt.get().getId())
            .distinct()
            .forEach(userId -> {
                try {
                    notifyFn.accept(userId);
                } catch (Exception e) {
                    log.warn("Failed to notify bidder {}: {}", userId, e.getMessage());
                }
            });
    }
}
