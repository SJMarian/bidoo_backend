package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.BidDto;
import com.example.bidoo_backend.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    /**
     * Place a bid on an auction.
     * POST /api/bids/auction/{auctionId}
     * Body: { "bidderUsername": "john@example.com", "amount": 1500.00 }
     */
    @PostMapping("/auction/{auctionId}")
    public ResponseEntity<BidDto.BidResponse> placeBid(
            @PathVariable Long auctionId,
            @RequestBody BidDto.PlaceBidRequest request) {
        return ResponseEntity.ok(bidService.placeBid(auctionId, request.getBidderUsername(), request.getAmount()));
    }

    /**
     * Get all bids for an auction (highest first).
     * GET /api/bids/auction/{auctionId}
     */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidDto.BidResponse>> getBidsForAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(bidService.getBidsForAuction(auctionId));
    }

    /**
     * Get bid history for an auction (latest first).
     * GET /api/bids/auction/{auctionId}/history
     */
    @GetMapping("/auction/{auctionId}/history")
    public ResponseEntity<List<BidDto.BidResponse>> getBidHistory(@PathVariable Long auctionId) {
        return ResponseEntity.ok(bidService.getBidHistory(auctionId));
    }

    /**
     * Get the current highest bid for an auction.
     * GET /api/bids/auction/{auctionId}/highest
     */
    @GetMapping("/auction/{auctionId}/highest")
    public ResponseEntity<BidDto.BidResponse> getHighestBid(@PathVariable Long auctionId) {
        return bidService.getHighestBid(auctionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
