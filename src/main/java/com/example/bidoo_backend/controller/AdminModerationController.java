package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.AdminModerationDto;
import com.example.bidoo_backend.service.AdminModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Admin Auction Moderation Tools.
 *
 * All endpoints are prefixed with /api/admin/auctions
 *
 * NOTE: In a full system these would be secured with Spring Security
 * so only ADMIN role can access them. The adminUsername would come
 * from the authenticated principal. For now it is taken from a
 * request header "X-Admin-Username" for simplicity.
 */
@RestController
@RequestMapping("/api/admin/auctions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly in production
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    private static final String ADMIN_HEADER = "X-Admin-Username";
    private static final String DEFAULT_ADMIN = "admin";

    // ── READ ─────────────────────────────────────────────────────────────────

    /** GET /api/admin/auctions — list all auctions */
    @GetMapping
    public ResponseEntity<List<AdminModerationDto.AuctionAdminResponse>> getAllAuctions() {
        return ResponseEntity.ok(adminModerationService.getAllAuctions());
    }

    /** GET /api/admin/auctions/pending — list all PENDING auctions */
    @GetMapping("/pending")
    public ResponseEntity<List<AdminModerationDto.AuctionAdminResponse>> getPendingAuctions() {
        return ResponseEntity.ok(adminModerationService.getPendingAuctions());
    }

    /** GET /api/admin/auctions/{id} — get a single auction */
    @GetMapping("/{id}")
    public ResponseEntity<AdminModerationDto.AuctionAdminResponse> getAuction(@PathVariable Long id) {
        return ResponseEntity.ok(adminModerationService.getAuctionById(id));
    }

    // ── MODERATION ACTIONS ────────────────────────────────────────────────────

    /** POST /api/admin/auctions/{id}/approve */
    @PostMapping("/{id}/approve")
    public ResponseEntity<AdminModerationDto.AuctionAdminResponse> approveAuction(
            @PathVariable Long id,
            @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN) String adminUsername) {
        return ResponseEntity.ok(adminModerationService.approveAuction(id, adminUsername));
    }

    /** POST /api/admin/auctions/{id}/reject */
    @PostMapping("/{id}/reject")
    public ResponseEntity<AdminModerationDto.AuctionAdminResponse> rejectAuction(
            @PathVariable Long id,
            @RequestBody AdminModerationDto.RejectRequest request,
            @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN) String adminUsername) {
        return ResponseEntity.ok(adminModerationService.rejectAuction(id, request, adminUsername));
    }

    /** POST /api/admin/auctions/{id}/cancel */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AdminModerationDto.AuctionAdminResponse> cancelAuction(
            @PathVariable Long id,
            @RequestBody AdminModerationDto.CancelRequest request,
            @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN) String adminUsername) {
        return ResponseEntity.ok(adminModerationService.cancelAuction(id, request, adminUsername));
    }

    /** POST /api/admin/auctions/{id}/block-bids */
    @PostMapping("/{id}/block-bids")
    public ResponseEntity<AdminModerationDto.AuctionAdminResponse> blockBids(
            @PathVariable Long id,
            @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN) String adminUsername) {
        return ResponseEntity.ok(adminModerationService.blockBids(id, adminUsername));
    }

    /** POST /api/admin/auctions/{id}/unblock-bids */
    @PostMapping("/{id}/unblock-bids")
    public ResponseEntity<AdminModerationDto.AuctionAdminResponse> unblockBids(
            @PathVariable Long id,
            @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN) String adminUsername) {
        return ResponseEntity.ok(adminModerationService.unblockBids(id, adminUsername));
    }

    /** POST /api/admin/auctions/{id}/close */
    @PostMapping("/{id}/close")
    public ResponseEntity<AdminModerationDto.AuctionAdminResponse> manuallyCloseAuction(
            @PathVariable Long id,
            @RequestBody AdminModerationDto.ManualCloseRequest request,
            @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN) String adminUsername) {
        return ResponseEntity.ok(adminModerationService.manuallyCloseAuction(id, request, adminUsername));
    }

    /** POST /api/admin/auctions/{id}/reopen */
    @PostMapping("/{id}/reopen")
    public ResponseEntity<AdminModerationDto.AuctionAdminResponse> reopenAuction(
            @PathVariable Long id,
            @RequestBody AdminModerationDto.ReopenRequest request,
            @RequestHeader(value = ADMIN_HEADER, defaultValue = DEFAULT_ADMIN) String adminUsername) {
        return ResponseEntity.ok(adminModerationService.reopenAuction(id, request, adminUsername));
    }

    // ── ACTION LOGS ───────────────────────────────────────────────────────────

    /** GET /api/admin/auctions/logs — all admin action logs */
    @GetMapping("/logs")
    public ResponseEntity<List<AdminModerationDto.AdminActionLogResponse>> getAllLogs() {
        return ResponseEntity.ok(adminModerationService.getAllLogs());
    }

    /** GET /api/admin/auctions/{id}/logs — logs for a specific auction */
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<AdminModerationDto.AdminActionLogResponse>> getLogsByAuction(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminModerationService.getLogsByAuction(id));
    }

    // ── GLOBAL EXCEPTION HANDLER ──────────────────────────────────────────────

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
