package com.example.bidoo_backend.service;

import com.example.bidoo_backend.dto.AdminModerationDto;
import com.example.bidoo_backend.entity.AdminActionLog;
import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.enums.AdminActionType;
import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.repository.AdminActionLogRepository;
import com.example.bidoo_backend.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminModerationService {

    private final AuctionRepository auctionRepository;
    private final AdminActionLogRepository adminActionLogRepository;

    // ── Read ────────────────────────────────────────────────────────────────

    /** All auctions (admin sees everything) */
    public List<AdminModerationDto.AuctionAdminResponse> getAllAuctions() {
        return auctionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Auctions awaiting review */
    public List<AdminModerationDto.AuctionAdminResponse> getPendingAuctions() {
        return auctionRepository.findByStatus(AuctionStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Single auction by id */
    public AdminModerationDto.AuctionAdminResponse getAuctionById(Long id) {
        Auction auction = findOrThrow(id);
        return toResponse(auction);
    }

    // ── Moderation Actions ───────────────────────────────────────────────────

    /** Approve a PENDING auction — moves it to UPCOMING */
    @Transactional
    public AdminModerationDto.AuctionAdminResponse approveAuction(Long auctionId, String adminUsername) {
        Auction auction = findOrThrow(auctionId);
        requireStatus(auction, AuctionStatus.PENDING, "Only PENDING auctions can be approved.");

        auction.setStatus(AuctionStatus.UPCOMING);
        auctionRepository.save(auction);
        logAction(auction, AdminActionType.APPROVE, null, adminUsername);
        return toResponse(auction);
    }

    /** Reject a PENDING auction with a mandatory reason */
    @Transactional
    public AdminModerationDto.AuctionAdminResponse rejectAuction(
            Long auctionId, AdminModerationDto.RejectRequest request, String adminUsername) {
        requireNonEmpty(request.getReason(), "Rejection reason is required.");
        Auction auction = findOrThrow(auctionId);
        requireStatus(auction, AuctionStatus.PENDING, "Only PENDING auctions can be rejected.");

        auction.setStatus(AuctionStatus.REJECTED);
        auction.setRejectionReason(request.getReason());
        auctionRepository.save(auction);
        logAction(auction, AdminActionType.REJECT, request.getReason(), adminUsername);
        return toResponse(auction);
    }

    /** Cancel an approved/upcoming/active auction with a reason */
    @Transactional
    public AdminModerationDto.AuctionAdminResponse cancelAuction(
            Long auctionId, AdminModerationDto.CancelRequest request, String adminUsername) {
        requireNonEmpty(request.getReason(), "Cancellation reason is required.");
        Auction auction = findOrThrow(auctionId);
        requireStatusIn(auction,
                List.of(AuctionStatus.UPCOMING, AuctionStatus.ACTIVE),
                "Only UPCOMING or ACTIVE auctions can be cancelled.");

        auction.setStatus(AuctionStatus.CANCELLED);
        auction.setCancellationReason(request.getReason());
        auctionRepository.save(auction);
        logAction(auction, AdminActionType.CANCEL, request.getReason(), adminUsername);
        return toResponse(auction);
    }

    /** Block bids for an ACTIVE auction */
    @Transactional
    public AdminModerationDto.AuctionAdminResponse blockBids(Long auctionId, String adminUsername) {
        Auction auction = findOrThrow(auctionId);
        requireStatus(auction, AuctionStatus.ACTIVE, "Only ACTIVE auctions can have bids blocked.");
        if (auction.isBidsBlocked()) {
            throw new IllegalStateException("Bids are already blocked for this auction.");
        }

        auction.setBidsBlocked(true);
        auctionRepository.save(auction);
        logAction(auction, AdminActionType.BLOCK_BIDS, null, adminUsername);
        return toResponse(auction);
    }

    /** Unblock bids for an ACTIVE auction that had bids blocked */
    @Transactional
    public AdminModerationDto.AuctionAdminResponse unblockBids(Long auctionId, String adminUsername) {
        Auction auction = findOrThrow(auctionId);
        requireStatus(auction, AuctionStatus.ACTIVE, "Only ACTIVE auctions can have bids unblocked.");
        if (!auction.isBidsBlocked()) {
            throw new IllegalStateException("Bids are not blocked for this auction.");
        }

        auction.setBidsBlocked(false);
        auctionRepository.save(auction);
        logAction(auction, AdminActionType.UNBLOCK_BIDS, null, adminUsername);
        return toResponse(auction);
    }

    /** Manually close an ACTIVE auction before its scheduled end time */
    @Transactional
    public AdminModerationDto.AuctionAdminResponse manuallyCloseAuction(
            Long auctionId, AdminModerationDto.ManualCloseRequest request, String adminUsername) {
        requireNonEmpty(request.getReason(), "Close reason is required.");
        Auction auction = findOrThrow(auctionId);
        requireStatus(auction, AuctionStatus.ACTIVE, "Only ACTIVE auctions can be manually closed.");

        auction.setStatus(AuctionStatus.CLOSED);
        auction.setEndAt(LocalDateTime.now()); // update end time to now
        auctionRepository.save(auction);
        logAction(auction, AdminActionType.MANUAL_CLOSE, request.getReason(), adminUsername);
        return toResponse(auction);
    }

    /** Reopen a CLOSED auction under controlled conditions with new end time */
    @Transactional
    public AdminModerationDto.AuctionAdminResponse reopenAuction(
            Long auctionId, AdminModerationDto.ReopenRequest request, String adminUsername) {
        if (request.getNewEndTime() == null) {
            throw new IllegalArgumentException("New end time is required to reopen an auction.");
        }
        if (request.getNewEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New end time must be in the future.");
        }
        Auction auction = findOrThrow(auctionId);
        requireStatus(auction, AuctionStatus.CLOSED, "Only CLOSED auctions can be reopened.");

        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setEndAt(request.getNewEndTime());
        auction.setBidsBlocked(false);
        auctionRepository.save(auction);
        logAction(auction, AdminActionType.REOPEN, request.getReason(), adminUsername);
        return toResponse(auction);
    }

    // ── Action Logs ──────────────────────────────────────────────────────────

    public List<AdminModerationDto.AdminActionLogResponse> getAllLogs() {
        return adminActionLogRepository.findAllByOrderByPerformedAtDesc()
                .stream()
                .map(this::toLogResponse)
                .collect(Collectors.toList());
    }

    public List<AdminModerationDto.AdminActionLogResponse> getLogsByAuction(Long auctionId) {
        return adminActionLogRepository.findByAuctionIdOrderByPerformedAtDesc(auctionId)
                .stream()
                .map(this::toLogResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Auction findOrThrow(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found with id: " + id));
    }

    private void requireStatus(Auction auction, AuctionStatus required, String message) {
        if (auction.getStatus() != required) {
            throw new IllegalStateException(message + " Current status: " + auction.getStatus());
        }
    }

    private void requireStatusIn(Auction auction, List<AuctionStatus> allowed, String message) {
        if (!allowed.contains(auction.getStatus())) {
            throw new IllegalStateException(message + " Current status: " + auction.getStatus());
        }
    }

    private void requireNonEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void logAction(Auction auction, AdminActionType actionType, String reason, String adminUsername) {
        AdminActionLog log = AdminActionLog.builder()
                .auctionId(auction.getId())
                .auctionTitle(auction.getTitle())
                .actionType(actionType)
                .reason(reason)
                .performedBy(adminUsername)
                .build();
        adminActionLogRepository.save(log);
    }

    private AdminModerationDto.AuctionAdminResponse toResponse(Auction auction) {
        return AdminModerationDto.AuctionAdminResponse.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .category(auction.getCategory())
                .description(auction.getDescription())
                .startingPrice(auction.getBidStartingPrice() != null ? java.math.BigDecimal.valueOf(auction.getBidStartingPrice()) : java.math.BigDecimal.ZERO)
                .minimumBidIncrement(auction.getMinimumBidIncrement() != null ? java.math.BigDecimal.valueOf(auction.getMinimumBidIncrement()) : java.math.BigDecimal.ZERO)
                .startTime(auction.getStartAt())
                .endTime(auction.getEndAt())
                .status(auction.getStatus())
                .sellerUsername(auction.getSeller() != null ? auction.getSeller().getName() : "Unknown")
                .rejectionReason(auction.getRejectionReason())
                .cancellationReason(auction.getCancellationReason())
                .bidsBlocked(auction.isBidsBlocked())
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .build();
    }

    private AdminModerationDto.AdminActionLogResponse toLogResponse(AdminActionLog log) {
        return AdminModerationDto.AdminActionLogResponse.builder()
                .id(log.getId())
                .auctionId(log.getAuctionId())
                .auctionTitle(log.getAuctionTitle())
                .actionType(log.getActionType())
                .reason(log.getReason())
                .performedBy(log.getPerformedBy())
                .performedAt(log.getPerformedAt())
                .build();
    }
}
