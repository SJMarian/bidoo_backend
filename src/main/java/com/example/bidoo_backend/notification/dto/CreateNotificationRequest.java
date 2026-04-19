package com.example.bidoo_backend.notification.dto;

import com.example.bidoo_backend.notification.entity.NotificationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {
    private Long userId;
    private NotificationType type;
    private String message;
    private String auctionTitle;
    private Long auctionId;
}
