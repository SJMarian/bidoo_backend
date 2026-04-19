package com.example.bidoo_backend.notification.repository;

import com.example.bidoo_backend.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a user, newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Get only unread notifications for a user
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    // Count unread notifications for a user (for badge)
    long countByUserIdAndReadFalse(Long userId);

    // Mark all notifications for a user as read
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.read = false")
    int markAllAsReadByUserId(Long userId);
}
