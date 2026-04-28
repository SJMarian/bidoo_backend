package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.AnalyticsDto;
import com.example.bidoo_backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Module 3 Analytics Features:
 *
 *  GET  /api/admin/analytics/dashboard          → Dashboard metrics
 *  GET  /api/admin/analytics/transactions        → All transaction records
 *  GET  /api/admin/analytics/transactions/{auctionId} → Transaction for one auction
 *  GET  /api/admin/analytics/invoice/{orderId}   → Invoice data for PDF generation
 *  POST /api/admin/analytics/determine-winner/{auctionId} → Determine auction winner
 *  POST /api/admin/analytics/determine-all-winners        → Batch determine expired winners
 *  GET  /api/auctions/{auctionId}/bid-growth     → Bid growth trend for one auction
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ── Dashboard ──────────────────────────────────────────────────────────────

    @GetMapping("/api/admin/analytics/dashboard")
    public ResponseEntity<AnalyticsDto.DashboardMetrics> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardMetrics());
    }

    // ── Winner Determination ──────────────────────────────────────────────────

    @PostMapping("/api/admin/analytics/determine-winner/{auctionId}")
    public ResponseEntity<AnalyticsDto.WinnerDeterminationResult> determineWinner(
            @PathVariable Long auctionId) {
        return ResponseEntity.ok(analyticsService.determineWinner(auctionId));
    }

    @PostMapping("/api/admin/analytics/determine-all-winners")
    public ResponseEntity<List<AnalyticsDto.WinnerDeterminationResult>> determineAllWinners() {
        return ResponseEntity.ok(analyticsService.determineAllExpiredAuctionWinners());
    }

    // ── Transaction Records ───────────────────────────────────────────────────

    @GetMapping("/api/admin/analytics/transactions")
    public ResponseEntity<List<AnalyticsDto.TransactionRecord>> getAllTransactions() {
        return ResponseEntity.ok(analyticsService.getAllTransactionRecords());
    }

    @GetMapping("/api/admin/analytics/transactions/{auctionId}")
    public ResponseEntity<AnalyticsDto.TransactionRecord> getTransactionByAuction(
            @PathVariable Long auctionId) {
        return ResponseEntity.ok(analyticsService.getTransactionByAuction(auctionId));
    }

    // ── Invoice ───────────────────────────────────────────────────────────────

    @GetMapping("/api/admin/analytics/invoice/{orderId}")
    public ResponseEntity<AnalyticsDto.InvoiceData> getInvoice(@PathVariable Long orderId) {
        return ResponseEntity.ok(analyticsService.getInvoice(orderId));
    }

    // ── Bid Growth Trend ──────────────────────────────────────────────────────

    @GetMapping("/api/auctions/{auctionId}/bid-growth")
    public ResponseEntity<AnalyticsDto.BidGrowthTrend> getBidGrowthTrend(
            @PathVariable Long auctionId) {
        return ResponseEntity.ok(analyticsService.getBidGrowthTrend(auctionId));
    }

    // ── Error Handling ────────────────────────────────────────────────────────

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
