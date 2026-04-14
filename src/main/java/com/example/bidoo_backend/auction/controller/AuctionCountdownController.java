package com.example.bidoo_backend.auction.controller;

import com.example.bidoo_backend.auction.dto.AuctionCountdownDTO;
import com.example.bidoo_backend.auction.dto.ServerTimeDTO;
import com.example.bidoo_backend.auction.entity.Auction;
import com.example.bidoo_backend.auction.entity.AuctionState;
import com.example.bidoo_backend.auction.service.AuctionLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the countdown & server-time sync feature.
 *
 * GET /api/time                          — current server time (for clock sync)
 * GET /api/auctions                      — all auctions
 * GET /api/auctions/{id}                 — single auction details
 * GET /api/auctions/{id}/countdown       — timing data for one auction
 *
 * WebSocket topics (subscribe from frontend):
 *   /topic/auction/{id}/state            — state change broadcasts (ACTIVE → CLOSED etc.)
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuctionCountdownController {

    private final AuctionLifecycleService lifecycleService;

    /**
     * Returns the current server time in milliseconds.
     * The frontend calls this once on load to calculate the offset
     * between the client clock and server clock, ensuring the countdown
     * is always server-synchronized regardless of client clock drift.
     */
    @GetMapping("/api/time")
    public ResponseEntity<ServerTimeDTO> getServerTime() {
        return ResponseEntity.ok(lifecycleService.getServerTime());
    }

    /**
     * Returns countdown data for a specific auction:
     * endTimeMs, startTimeMs, serverTimeMs, remainingMs, and current state.
     * The frontend uses this to initialize the countdown timer.
     */
    @GetMapping("/api/auctions/{id}/countdown")
    public ResponseEntity<AuctionCountdownDTO> getCountdown(@PathVariable Long id) {
        return ResponseEntity.ok(lifecycleService.getCountdown(id));
    }

    /**
     * Returns all auctions in the system
     */
    @GetMapping("/api/auctions")
    public ResponseEntity<List<Auction>> getAllAuctions() {
        return ResponseEntity.ok(lifecycleService.getAllAuctions());
    }

    /**
     * Returns a single auction by ID
     */
    @GetMapping("/api/auctions/{id}")
    public ResponseEntity<Auction> getAuction(@PathVariable Long id) {
        return ResponseEntity.ok(lifecycleService.getAuctionById(id));
    }
}
