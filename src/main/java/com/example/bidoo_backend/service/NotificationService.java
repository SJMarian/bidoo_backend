package com.example.bidoo_backend.service;

import com.example.bidoo_backend.dto.NotificationDTO;
import com.example.bidoo_backend.entity.Notification;
import com.example.bidoo_backend.enums.NotificationStatus;
import com.example.bidoo_backend.enums.NotificationType;
import com.example.bidoo_backend.enums.SeverityLevel;
import com.example.bidoo_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public NotificationDTO createNotification(Long userId, String title, String message, 
                                              NotificationType type, SeverityLevel severity) {
        return createNotification(userId, title, message, type, severity, null, null);
    }
    
    public NotificationDTO createNotification(Long userId, String title, String message,
                                              NotificationType type, SeverityLevel severity,
                                              Long relatedAuctionId, Long relatedBidId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setSeverity(severity);
        notification.setStatus(NotificationStatus.SENT);
        notification.setIsRead(false);
        notification.setRelatedAuctionId(relatedAuctionId);
        notification.setRelatedBidId(relatedBidId);
        
        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }
    
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }
    
    public NotificationDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);
        return convertToDTO(updated);
    }
    
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    public void deleteNotificationsByUser(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }
    
    public List<NotificationDTO> getNotificationsByType(Long userId, NotificationType type) {
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getType(),
                notification.getSeverity(),
                notification.getIsRead(),
                notification.getRelatedAuctionId(),
                notification.getRelatedBidId(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
