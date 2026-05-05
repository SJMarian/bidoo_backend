package com.example.bidoo_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.bidoo_backend.enums.NotificationStatus;
import com.example.bidoo_backend.enums.NotificationType;
import com.example.bidoo_backend.enums.SeverityLevel;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = true)
    private String title;
    
    @Column(length = 500)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private NotificationStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private SeverityLevel severity;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "related_auction_id")
    private Long relatedAuctionId;
    
    @Column(name = "related_bid_id")
    private Long relatedBidId;
    
    @Column(nullable = true, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = true)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
