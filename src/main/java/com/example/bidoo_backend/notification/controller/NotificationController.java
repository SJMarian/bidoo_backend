package com.example.bidoo_backend.notification.controller;

import com.example.bidoo_backend.notification.dto.*;
import com.example.bidoo_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the In-System Notification & Alert System.
 *
 * All endpoints are prefixed with /api/notifications.
 * userId is passed as a request parameter (would be extracted from JWT token in full auth setup).
 *
 * Endpoints:
 *   GET    /api/notifications?userId=X           - Get all notifications for user
 *   GET    /api/notifications/unread?userId=X    - Get unread notifications
 *   GET    /api/notifications/count?userId=X     - Get unread count (for badge)
 *   POST   /api/notifications                    - Create & send a notification (internal/admin use)
 *   PATCH  /api/notifications/{id}/read?userId=X - Mark one as read
 *   PATCH  /api/notifications/read-all?userId=X  - Mark all as read
 *   DELETE /api/notifications/{id}?userId=X      - Delete a notification
 *
 * WebSocket topic (per user):
 *   /user/{userId}/queue/notifications           - Real-time push of new NotificationDTO
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for a user (newest first).
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(
            @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getAllForUser(userId));
    }

    /**
     * Get only unread notifications for a user.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadForUser(userId));
    }

    /**
     * Get the unread notification count for the dashboard badge.
     */
    @GetMapping("/count")
    public ResponseEntity<UnreadCountDTO> getUnreadCount(
            @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    /**
     * Create and send a notification. Intended for internal service calls
     * (e.g., triggered when an auction closes, a bid is placed, etc.).
     */
    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(
            @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.ok(notificationService.createAndSend(request));
    }

    /**
     * Mark a single notification as read.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    /**
     * Mark all notifications as read for a user.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @RequestParam Long userId) {
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("updated", updated, "message", "All notifications marked as read"));
    }

    /**
     * Accept a notification.
     */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<NotificationDTO> acceptNotification(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.acceptNotification(id, userId));
    }

    /**
     * Reject a notification.
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<NotificationDTO> rejectNotification(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.rejectNotification(id, userId));
    }

    /**
     * Delete a notification.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @RequestParam Long userId) {
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
    }
}
