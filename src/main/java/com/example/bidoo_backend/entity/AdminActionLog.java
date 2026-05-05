package com.example.bidoo_backend.entity;

import com.example.bidoo_backend.enums.AdminActionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_action_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long auctionId;

    @Column(nullable = false)
    private String auctionTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminActionType actionType;

    private String reason;

    @Column(nullable = false)
    private String performedBy; // admin username

    @Column(nullable = false)
    private LocalDateTime performedAt;

    @PrePersist
    protected void onCreate() {
        performedAt = LocalDateTime.now();
    }
}
