package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.BidDto;
import com.example.bidoo_backend.service.BidIncrementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for the Bid Increment Rule Engine.
 *
 * Endpoints:
 *   GET  /api/auctions/{id}/bid-state          → current bid state + minimum next bid
 *   GET  /api/auctions/{id}/bids               → full bid history
 *   GET  /api/auctions/{id}/validate-bid        → pre-validate a bid amount (query param)
 *   POST /api/auctions/{id}/bids               → place a bid
 *   PUT  /api/admin/auctions/{id}/increment-rule → admin: update the increment rule
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BidIncrementController {

    private final BidIncrementService bidIncrementService;

    /** Current bid state — used by frontend to show highest bid + compute minimum */
    @GetMapping("/api/auctions/{id}/bid-state")
    public ResponseEntity<BidDto.BidStateResponse> getBidState(@PathVariable Long id) {
        return ResponseEntity.ok(bidIncrementService.getBidState(id));
    }

    /** Full bid history for an auction, newest first */
    @GetMapping("/api/auctions/{id}/bids")
    public ResponseEntity<List<BidDto.BidResponse>> getBidHistory(@PathVariable Long id) {
        return ResponseEntity.ok(bidIncrementService.getBidHistory(id));
    }

    /**
     * Pre-validate a bid amount before submitting.
     * Call this as user types to show real-time feedback.
     * GET /api/auctions/{id}/validate-bid?amount=150.00
     */
    @GetMapping("/api/auctions/{id}/validate-bid")
    public ResponseEntity<BidDto.BidValidationResponse> validateBid(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(bidIncrementService.prevalidateBid(id, amount));
    }

    /** Place a bid — enforces the increment rule server-side */
    @PostMapping("/api/auctions/{id}/bids")
    public ResponseEntity<BidDto.BidResponse> placeBid(
            @PathVariable Long id,
            @RequestBody BidDto.PlaceBidRequest request) {
        return ResponseEntity.ok(bidIncrementService.placeBid(id, request));
    }

    /** Admin: update the bid increment rule for an auction */
    @PutMapping("/api/admin/auctions/{id}/increment-rule")
    public ResponseEntity<BidDto.BidStateResponse> updateIncrementRule(
            @PathVariable Long id,
            @RequestBody BidDto.UpdateIncrementRuleRequest request) {
        return ResponseEntity.ok(bidIncrementService.updateIncrementRule(id, request));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
