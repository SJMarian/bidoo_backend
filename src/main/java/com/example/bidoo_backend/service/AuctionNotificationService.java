package com.example.bidoo_backend.service;

import com.example.bidoo_backend.dto.NotificationDTO;
import com.example.bidoo_backend.enums.NotificationType;
import com.example.bidoo_backend.enums.SeverityLevel;
import com.example.bidoo_backend.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionNotificationService {

    private final NotificationService notificationService;
    private final NotificationWebSocketHandler webSocketHandler;

    // Admin user ID - always receives a copy of every notification
    private static final Long ADMIN_USER_ID = 8L;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    // ── Bid Events ────────────────────────────────────────────────────────────

    public void notifyBidPlaced(Long bidderId, String auctionTitle, Double bidAmount,
                                Long auctionId, Long bidId) {
        String title = "✓ Bid Placed Successfully";
        String message = String.format("Bid of $%.2f placed on \"%s\". You are the highest bidder.",
                bidAmount, auctionTitle);
        sendToUser(bidderId, title, message, NotificationType.BID_PLACED, SeverityLevel.SUCCESS, auctionId, bidId);
        sendToAdmin(title, String.format("User #%d placed $%.2f on \"%s\"", bidderId, bidAmount, auctionTitle),
                NotificationType.BID_PLACED, SeverityLevel.INFO, auctionId);
    }

    public void notifyOutbid(Long outbidUserId, String auctionTitle, Double newBidAmount, Long auctionId) {
        String title = "📉 You've Been Outbid";
        String message = String.format("Your bid on \"%s\" was exceeded. New highest bid: $%.2f. Bid again to stay in the lead!",
                auctionTitle, newBidAmount);
        sendToUser(outbidUserId, title, message, NotificationType.BID_OUTBID, SeverityLevel.WARNING, auctionId, null);
        sendToAdmin(title, String.format("User #%d was outbid on \"%s\". New bid: $%.2f", outbidUserId, auctionTitle, newBidAmount),
                NotificationType.BID_OUTBID, SeverityLevel.INFO, auctionId);
    }

    // ── Auction Status Events ─────────────────────────────────────────────────

    public void notifyAuctionApproved(Long sellerId, String auctionTitle, Long auctionId) {
        String title = "✅ Auction Approved";
        String message = String.format("Your auction \"%s\" has been approved and is now visible to bidders.", auctionTitle);
        sendToUser(sellerId, title, message, NotificationType.AUCTION_APPROVED, SeverityLevel.SUCCESS, auctionId, null);
        sendToAdmin(title, String.format("Auction \"%s\" (seller #%d) was approved.", auctionTitle, sellerId),
                NotificationType.AUCTION_APPROVED, SeverityLevel.SUCCESS, auctionId);
    }

    public void notifyAuctionRejected(Long sellerId, String auctionTitle, String reason, Long auctionId) {
        String title = "❌ Auction Rejected";
        String message = String.format("Your auction \"%s\" was rejected. Reason: %s", auctionTitle, reason);
        sendToUser(sellerId, title, message, NotificationType.AUCTION_REJECTED, SeverityLevel.ERROR, auctionId, null);
        sendToAdmin(title, String.format("Auction \"%s\" rejected. Reason: %s", auctionTitle, reason),
                NotificationType.AUCTION_REJECTED, SeverityLevel.WARNING, auctionId);
    }

    public void notifyAuctionCancelled(Long sellerId, String auctionTitle, String reason, Long auctionId) {
        String title = "🚫 Auction Cancelled";
        String message = String.format("Your auction \"%s\" has been cancelled. Reason: %s", auctionTitle, reason);
        sendToUser(sellerId, title, message, NotificationType.AUCTION_CANCELLED, SeverityLevel.ERROR, auctionId, null);
        sendToAdmin(title, String.format("Auction \"%s\" cancelled. Reason: %s", auctionTitle, reason),
                NotificationType.AUCTION_CANCELLED, SeverityLevel.WARNING, auctionId);
    }

    public void notifyBidderAuctionCancelled(Long bidderId, String auctionTitle, String reason, Long auctionId) {
        String title = "🚫 Auction Cancelled";
        String message = String.format("The auction \"%s\" you bid on has been cancelled. Reason: %s", auctionTitle, reason);
        sendToUser(bidderId, title, message, NotificationType.AUCTION_CANCELLED, SeverityLevel.WARNING, auctionId, null);
    }

    public void notifyAuctionWinner(Long winnerId, String auctionTitle, Double winAmount, Long auctionId) {
        String title = "🎉 You Won the Auction!";
        String message = String.format("Congratulations! You won \"%s\" for $%.2f. Please complete payment within 7 days.",
                auctionTitle, winAmount);
        sendToUser(winnerId, title, message, NotificationType.YOU_WON_AUCTION, SeverityLevel.SUCCESS, auctionId, null);
        sendToAdmin(title, String.format("User #%d won \"%s\" for $%.2f.", winnerId, auctionTitle, winAmount),
                NotificationType.YOU_WON_AUCTION, SeverityLevel.SUCCESS, auctionId);
    }

    public void notifyAuctionLost(Long bidderId, String auctionTitle, Long auctionId) {
        String title = "🏁 Auction Ended";
        String message = String.format("The auction \"%s\" has ended. Unfortunately you were not the winning bidder.", auctionTitle);
        sendToUser(bidderId, title, message, NotificationType.AUCTION_CLOSED, SeverityLevel.INFO, auctionId, null);
    }

    public void notifyAuctionEndingSoon(Long userId, String auctionTitle, Long auctionId) {
        String title = "⏰ Auction Ending Soon";
        String message = String.format("\"%s\" will end in 1 hour. Place your final bid now!", auctionTitle);
        sendToUser(userId, title, message, NotificationType.AUCTION_ENDING_SOON, SeverityLevel.WARNING, auctionId, null);
        sendToAdmin(title, String.format("Auction \"%s\" is ending in 1 hour.", auctionTitle),
                NotificationType.AUCTION_ENDING_SOON, SeverityLevel.INFO, auctionId);
    }

    public void notifyBidsBlocked(Long userId, String auctionTitle, Long auctionId) {
        String title = "🔒 Bidding Suspended";
        String message = String.format("Bidding on \"%s\" has been temporarily suspended by an administrator.", auctionTitle);
        sendToUser(userId, title, message, NotificationType.AUCTION_MODERATED, SeverityLevel.WARNING, auctionId, null);
        sendToAdmin(title, String.format("Bidding suspended on \"%s\".", auctionTitle),
                NotificationType.AUCTION_MODERATED, SeverityLevel.WARNING, auctionId);
    }

    public void notifyBidsUnblocked(Long userId, String auctionTitle, Long auctionId) {
        String title = "🔓 Bidding Resumed";
        String message = String.format("Bidding on \"%s\" has resumed. You can now place bids again.", auctionTitle);
        sendToUser(userId, title, message, NotificationType.AUCTION_MODERATED, SeverityLevel.SUCCESS, auctionId, null);
        sendToAdmin(title, String.format("Bidding resumed on \"%s\".", auctionTitle),
                NotificationType.AUCTION_MODERATED, SeverityLevel.SUCCESS, auctionId);
    }

    public void notifyAuctionReopened(Long userId, String auctionTitle, LocalDateTime newEndTime, Long auctionId) {
        String title = "🔄 Auction Reopened";
        String message = String.format("The auction \"%s\" has been reopened. New end time: %s.",
                auctionTitle, newEndTime.format(DATE_FMT));
        sendToUser(userId, title, message, NotificationType.AUCTION_MODERATED, SeverityLevel.INFO, auctionId, null);
        sendToAdmin(title, String.format("Auction \"%s\" reopened until %s.", auctionTitle, newEndTime.format(DATE_FMT)),
                NotificationType.AUCTION_MODERATED, SeverityLevel.INFO, auctionId);
    }

    // ── Payment Events ────────────────────────────────────────────────────────

    public void notifyPaymentRequired(Long userId, Double amount, String auctionTitle, Long auctionId) {
        String title = "💳 Payment Required";
        String message = String.format("Payment of $%.2f required for \"%s\". Due within 7 days.", amount, auctionTitle);
        sendToUser(userId, title, message, NotificationType.PAYMENT_REQUIRED, SeverityLevel.WARNING, auctionId, null);
        sendToAdmin(title, String.format("User #%d owes $%.2f for \"%s\".", userId, amount, auctionTitle),
                NotificationType.PAYMENT_REQUIRED, SeverityLevel.INFO, auctionId);
    }

    public void notifyPaymentReceived(Long userId, Double amount, String auctionTitle, Long auctionId) {
        String title = "✓ Payment Received";
        String message = String.format("Payment of $%.2f received for \"%s\". Thank you!", amount, auctionTitle);
        sendToUser(userId, title, message, NotificationType.PAYMENT_RECEIVED, SeverityLevel.SUCCESS, auctionId, null);
        sendToAdmin(title, String.format("Payment of $%.2f received from user #%d for \"%s\".", amount, userId, auctionTitle),
                NotificationType.PAYMENT_RECEIVED, SeverityLevel.SUCCESS, auctionId);
    }

    public void notifySuspiciousActivity(Long adminId, String message, Long auctionId) {
        sendToUser(adminId, "🚨 Suspicious Activity Detected", message,
                NotificationType.SUSPICIOUS_ACTIVITY, SeverityLevel.ERROR, auctionId, null);
    }

    // ── Internal Helpers ──────────────────────────────────────────────────────

    /** Send to a specific user AND push via WebSocket */
    private void sendToUser(Long userId, String title, String message,
                            NotificationType type, SeverityLevel severity,
                            Long auctionId, Long bidId) {
        try {
            NotificationDTO notification = notificationService.createNotification(
                    userId, title, message, type, severity, auctionId, bidId);
            webSocketHandler.sendNotificationToUser(userId, notification);
            log.info("Notification sent → user={}, type={}, title={}", userId, type, title);
        } catch (Exception e) {
            log.error("Failed to send notification to user={}: {}", userId, e.getMessage());
        }
    }

    /** Always send a copy to the admin (user ID 8) unless admin IS the target user */
    private void sendToAdmin(String title, String message,
                             NotificationType type, SeverityLevel severity, Long auctionId) {
        try {
            NotificationDTO notification = notificationService.createNotification(
                    ADMIN_USER_ID, title, message, type, severity, auctionId, null);
            webSocketHandler.sendNotificationToUser(ADMIN_USER_ID, notification);
            log.info("Admin notification sent → type={}, title={}", type, title);
        } catch (Exception e) {
            log.error("Failed to send admin notification: {}", e.getMessage());
        }
    }
}
