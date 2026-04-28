package com.example.bidoo_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String username;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private String userAgent;
    
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private Long relatedAuctionId;
    private Long relatedBidId;
    private Long relatedOrderId;
    
    public enum ActivityType {
        LOGIN,
        LOGOUT,
        BID_PLACED,
        BID_CANCELLED,
        AUCTION_CREATED,
        AUCTION_CANCELLED,
        AUCTION_VIEWED,
        PAYMENT_INITIATED,
        PAYMENT_COMPLETED,
        DASHBOARD_ACCESSED,
        PROFILE_UPDATED,
        REPORT_EXPORTED,
        ADMIN_ACTION,
        ERROR_OCCURRED
    }
}
