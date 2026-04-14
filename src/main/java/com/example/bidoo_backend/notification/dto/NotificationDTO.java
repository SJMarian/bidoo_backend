package com.example.bidoo_backend.notification.dto;

import com.example.bidoo_backend.notification.entity.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String message;
    private String auctionTitle;
    private Long auctionId;
    private boolean read;
    private Boolean accepted;
    private Boolean rejected;
    private LocalDateTime createdAt;
}
