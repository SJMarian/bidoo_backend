package com.example.bidoo_backend.service;

import com.example.bidoo_backend.dto.AdminModerationDto;
import com.example.bidoo_backend.entity.AdminActionLog;
import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.enums.AdminActionType;
import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.repository.AdminActionLogRepository;
import com.example.bidoo_backend.repository.AuctionRepository;
import com.example.bidoo_backend.repository.BidRepository;
import com.example.bidoo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminModerationService {

    private final AuctionRepository auctionRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final AuctionNotificationService auctionNotificationService;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;

    // ── Read ────────────────────────────────────────────────────────────────

    public List<AdminModerationDto.AuctionAdminResponse> getAllAuctions() {
        return auctionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AdminModerationDto.AuctionAdminResponse> getPendingAuctions() {
        return auctionRepository.findByStatus(AuctionStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AdminModerationDto.AuctionAdminResponse getAuctionById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ── Moderation Actions ───────────────────────────────────────────────────

    /** Approve a PENDING auction → UPCOMING. Notifies seller. */
    @Transactional
    public AdminModerationDto.AuctionAdminResponse approveAuction(Long auctionId, String adminUsername) {
        Auction auction = findOrThrow(auctionId);
        requireStatus(auction, AuctionStatus.PENDING, "Only PENDING auctions can be approved.");

        auction.setStatus(AuctionStatus.UPCOMING);
        auctionRepository.save(auction);
        logAction(auction, AdminActionType.APPROVE, null, adminUsername);

        // Notify seller
        notifySeller(auction, () ->
            auctionNotificationService.notifyAuctionApproved(
                auction.getSeller().getId(), auction.getTitle(), auction.getId())
        );

        return toResponse(auction);
    }

    /** Reject a PENDING auction. Notifies seller with reason. */
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

        // Notify seller
        notifySeller(auction, () ->
            auctionNotificationService.notifyAuctionRejected(
                auction.getSeller().getId(), auction.getTitle(), request.getReason(), auction.getId())
        );

        return toResponse(auction);
    }

    /** Cancel an UPCOMING or ACTIVE auction. Notifies seller + all bidders. */
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

        // Notify seller
        notifySeller(auction, () ->
            auctionNotificationService.notifyAuctionCancelled(
                auction.getSeller().getId(), auction.getTitle(), request.getReason(), auction.getId())
        );

        // Notify all bidders
        notifyAllBidders(auction, (userId) ->
            auctionNotificationService.notifyBidderAuctionCancelled(
                userId, auction.getTitle(), request.getReason(), auction.getId())
        );

        return toResponse(auction);
    }

    /** Block bids for an ACTIVE auction. Notifies all bidders. */
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

        // Notify all bidders
        notifyAllBidders(auction, (userId) ->
            auctionNotificationService.notifyBidsBlocked(userId, auction.getTitle(), auction.getId())
        );

        return toResponse(auction);
    }

    /** Unblock bids for an ACTIVE auction. Notifies all bidders. */
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

        // Notify all bidders that bidding has resumed
        notifyAllBidders(auction, (userId) ->
            auctionNotificationService.notifyBidsUnblocked(userId, auction.getTitle(), auction.getId())
        );

        return toResponse(auction);
    }

    /** Manually close an ACTIVE auction. Notifies winner + all bidders. */
    @Transactional
    public AdminModerationDto.AuctionAdminResponse manuallyCloseAuction(
            Long auctionId, AdminModerationDto.ManualCloseRequest request, String adminUsername) {
        requireNonEmpty(request.getReason(), "Close reason is required.");
        Auction auction = findOrThrow(auctionId);
        requireStatus(auction, AuctionStatus.ACTIVE, "Only ACTIVE auctions can be manually closed.");

        auction.setStatus(AuctionStatus.CLOSED);
        auction.setEndAt(LocalDateTime.now());
        auctionRepository.save(auction);
        logAction(auction, AdminActionType.MANUAL_CLOSE, request.getReason(), adminUsername);

        // Notify winner (highest bidder)
        if (auction.getCurrentHighestBidder() != null) {
            Long winnerId = auction.getCurrentHighestBidder().getId();
            Double winAmount = auction.getCurrentHighestBid();
            try {
                auctionNotificationService.notifyAuctionWinner(
                    winnerId, auction.getTitle(), winAmount, auctionId);
            } catch (Exception e) {
                log.warn("Failed to notify winner {}: {}", winnerId, e.getMessage());
            }
        }

        // Notify losing bidders
        notifyLosingBidders(auction);

        return toResponse(auction);
    }

    /** Reopen a CLOSED auction. Notifies all previous bidders. */
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

        // Notify all previous bidders that auction has reopened
        notifyAllBidders(auction, (userId) ->
            auctionNotificationService.notifyAuctionReopened(
                userId, auction.getTitle(), request.getNewEndTime(), auction.getId())
        );

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

    // ── Notification Helpers ─────────────────────────────────────────────────

    private void notifySeller(Auction auction, Runnable notifyFn) {
        if (auction.getSeller() == null) return;
        try {
            notifyFn.run();
        } catch (Exception e) {
            log.warn("Failed to notify seller for auction {}: {}", auction.getId(), e.getMessage());
        }
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

    private void notifyLosingBidders(Auction auction) {
        Long winnerId = auction.getCurrentHighestBidder() != null
                ? auction.getCurrentHighestBidder().getId() : null;
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

    // ── Internal Helpers ─────────────────────────────────────────────────────

    private Auction findOrThrow(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + id));
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
                .startingPrice(auction.getBidStartingPrice() != null
                        ? java.math.BigDecimal.valueOf(auction.getBidStartingPrice())
                        : java.math.BigDecimal.ZERO)
                .minimumBidIncrement(auction.getMinimumBidIncrement() != null
                        ? java.math.BigDecimal.valueOf(auction.getMinimumBidIncrement())
                        : java.math.BigDecimal.ZERO)
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
