package com.example.bidoo_backend.notification.service;

import com.example.bidoo_backend.notification.dto.*;
import com.example.bidoo_backend.notification.entity.Notification;
import com.example.bidoo_backend.notification.entity.NotificationType;
import com.example.bidoo_backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create and persist a notification, then push it in real-time via WebSocket.
     */
    @Transactional
    public NotificationDTO createAndSend(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .message(request.getMessage())
                .auctionTitle(request.getAuctionTitle())
                .auctionId(request.getAuctionId())
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationDTO dto = toDTO(saved);

        // Push real-time notification to the specific user's WebSocket topic
        messagingTemplate.convertAndSendToUser(
                String.valueOf(request.getUserId()),
                "/queue/notifications",
                dto
        );

        log.info("Notification sent to user {}: [{}] {}", request.getUserId(), request.getType(), request.getMessage());
        return dto;
    }

    /**
     * Convenience method: notify a winner that they won an auction and must pay.
     */
    @Transactional
    public NotificationDTO notifyAuctionWinner(Long winnerId, Long auctionId, String auctionTitle) {
        String message = String.format(
                "Congratulations! You won the auction for \"%s\". Please complete your payment to claim the item.",
                auctionTitle
        );
        return createAndSend(CreateNotificationRequest.builder()
                .userId(winnerId)
                .type(NotificationType.AUCTION_WON)
                .message(message)
                .auctionTitle(auctionTitle)
                .auctionId(auctionId)
                .build());
    }

    /**
     * Notify a bidder that someone outbid them.
     */
    @Transactional
    public NotificationDTO notifyOutbid(Long userId, Long auctionId, String auctionTitle, double newBidAmount) {
        String message = String.format(
                "You've been outbid on \"%s\". Current highest bid is $%.2f. Place a new bid to stay in the race!",
                auctionTitle, newBidAmount
        );
        return createAndSend(CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.OUTBID)
                .message(message)
                .auctionTitle(auctionTitle)
                .auctionId(auctionId)
                .build());
    }

    /**
     * Notify a seller that their auction was approved by admin.
     */
    @Transactional
    public NotificationDTO notifyAuctionApproved(Long sellerId, Long auctionId, String auctionTitle) {
        String message = String.format(
                "Your auction listing \"%s\" has been approved and is now live for bidding.",
                auctionTitle
        );
        return createAndSend(CreateNotificationRequest.builder()
                .userId(sellerId)
                .type(NotificationType.AUCTION_APPROVED)
                .message(message)
                .auctionTitle(auctionTitle)
                .auctionId(auctionId)
                .build());
    }

    /**
     * Notify a seller that their auction was rejected by admin.
     */
    @Transactional
    public NotificationDTO notifyAuctionRejected(Long sellerId, Long auctionId, String auctionTitle, String reason) {
        String message = String.format(
                "Your auction listing \"%s\" was rejected. Reason: %s",
                auctionTitle, reason
        );
        return createAndSend(CreateNotificationRequest.builder()
                .userId(sellerId)
                .type(NotificationType.AUCTION_REJECTED)
                .message(message)
                .auctionTitle(auctionTitle)
                .auctionId(auctionId)
                .build());
    }

    /**
     * Confirm a successful bid placement to the bidder.
     */
    @Transactional
    public NotificationDTO notifyBidPlaced(Long userId, Long auctionId, String auctionTitle, double bidAmount) {
        String message = String.format(
                "Your bid of $%.2f on \"%s\" was placed successfully. You are currently the highest bidder!",
                bidAmount, auctionTitle
        );
        return createAndSend(CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.BID_PLACED)
                .message(message)
                .auctionTitle(auctionTitle)
                .auctionId(auctionId)
                .build());
    }

    /**
     * Get all notifications for a user.
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllForUser(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get only unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadForUser(Long userId) {
        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread count for a user (for dashboard badge).
     */
    @Transactional(readOnly = true)
    public UnreadCountDTO getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return new UnreadCountDTO(count);
    }

    /**
     * Mark a single notification as read.
     */
    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: notification does not belong to user");
        }

        notification.setRead(true);
        return toDTO(notificationRepository.save(notification));
    }

    /**
     * Mark all notifications as read for a user.
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    /**
     * Delete a single notification (user action).
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: notification does not belong to user");
        }

        notificationRepository.delete(notification);
    }

    // --- Mapper ---

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .type(n.getType())
                .message(n.getMessage())
                .auctionTitle(n.getAuctionTitle())
                .auctionId(n.getAuctionId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
