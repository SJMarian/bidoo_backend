package com.example.bidoo_backend.auction.service;

import com.example.bidoo_backend.auction.dto.AuctionCountdownDTO;
import com.example.bidoo_backend.auction.dto.AuctionStateChangeDTO;
import com.example.bidoo_backend.auction.dto.ServerTimeDTO;
import com.example.bidoo_backend.auction.entity.Auction;
import com.example.bidoo_backend.auction.entity.AuctionState;
import com.example.bidoo_backend.auction.repository.AuctionRepository;
import com.example.bidoo_backend.notification.event.AuctionClosedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionRealtimeService {

    private final AuctionRepository auctionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    // ── Server Time ────────────────────────────────────────────────────────────

    /**
     * Returns the current server time. The frontend calls this once on page load
     * to calculate the clock offset between client and server.
     */
    public ServerTimeDTO getServerTime() {
        long nowMs = Instant.now().toEpochMilli();
        return ServerTimeDTO.builder()
                .serverTimeMs(nowMs)
                .serverTimeIso(DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(nowMs)))
                .build();
    }

    // ── Countdown Data ─────────────────────────────────────────────────────────

    /**
     * Returns timing data for a specific auction so the frontend can render
     * a server-synchronized countdown timer.
     */
    @Transactional(readOnly = true)
    public AuctionCountdownDTO getCountdown(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found: " + auctionId));

        long nowMs = Instant.now().toEpochMilli();
        long endMs = auction.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli();
        long startMs = auction.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
        long remainingMs = Math.max(0, endMs - nowMs);

        return AuctionCountdownDTO.builder()
                .auctionId(auction.getId())
                .auctionTitle(auction.getTitle())
                .state(auction.getState())
                .endTimeMs(endMs)
                .startTimeMs(startMs)
                .serverTimeMs(nowMs)
                .remainingMs(remainingMs)
                .build();
    }

    // ── State Transitions ──────────────────────────────────────────────────────

    /**
     * Activates all UPCOMING auctions whose start time has passed.
     * Called every 30 seconds by the scheduler.
     */
    @Transactional
    public void activateDueAuctions() {
        List<Auction> toActivate = auctionRepository.findAuctionsToActivate(LocalDateTime.now(ZoneOffset.UTC));
        for (Auction auction : toActivate) {
            AuctionState previous = auction.getState();
            auction.setState(AuctionState.ACTIVE);
            auctionRepository.save(auction);
            log.info("Auction {} '{}' activated", auction.getId(), auction.getTitle());
            broadcastStateChange(auction, previous, AuctionState.ACTIVE);
        }
    }

    /**
     * Closes all ACTIVE auctions whose end time has passed.
     * Called every 10 seconds by the scheduler.
     * Also fires the AuctionClosedEvent so the notification system sends winner alerts.
     */
    @Transactional
    public void closeExpiredAuctions() {
        List<Auction> toClose = auctionRepository.findAuctionsToClose(LocalDateTime.now(ZoneOffset.UTC));
        for (Auction auction : toClose) {
            AuctionState previous = auction.getState();
            auction.setState(AuctionState.CLOSED);
            auctionRepository.save(auction);
            log.info("Auction {} '{}' closed. Winner: {}", auction.getId(), auction.getTitle(), auction.getHighestBidderId());

            // Broadcast state change over WebSocket so all viewers see the closing animation
            broadcastStateChange(auction, previous, AuctionState.CLOSED);

            // Fire Spring event — the notification system handles winner alerts
            eventPublisher.publishEvent(new AuctionClosedEvent(
                    this,
                    auction.getId(),
                    auction.getTitle(),
                    auction.getHighestBidderId(),
                    auction.getCurrentHighestBid()
            ));
        }
    }

    // ── WebSocket Broadcast ────────────────────────────────────────────────────

    /**
     * Broadcasts an auction state change to all clients watching that auction.
     * Frontend subscribes to: /topic/auction/{auctionId}/state
     */
    private void broadcastStateChange(Auction auction, AuctionState previous, AuctionState next) {
        AuctionStateChangeDTO dto = AuctionStateChangeDTO.builder()
                .auctionId(auction.getId())
                .previousState(previous)
                .newState(next)
                .winnerId(auction.getHighestBidderId())
                .winningBid(auction.getCurrentHighestBid())
                .serverTimeMs(Instant.now().toEpochMilli())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/auction/" + auction.getId() + "/state",
                dto
        );
    }
}
