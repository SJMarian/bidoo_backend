package com.example.bidoo_backend.dto;

import com.example.bidoo_backend.enums.NotificationStatus;
import com.example.bidoo_backend.enums.NotificationType;
import com.example.bidoo_backend.enums.SeverityLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationStatus status;
    private NotificationType type;
    private SeverityLevel severity;
    private Boolean isRead;
    private Long relatedAuctionId;
    private Long relatedBidId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
