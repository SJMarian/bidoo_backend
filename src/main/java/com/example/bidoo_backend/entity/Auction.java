package com.example.bidoo_backend.entity;

import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.enums.BidIncrementType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Updated Auction entity.
 * Adds bidIncrementType (FIXED or PERCENTAGE) to support the
 * Bid Increment Rule Engine (Module 2).
 *
 * REPLACE your existing Auction.java with this file.
 */
@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal startingPrice;

    @Column(nullable = false)
    private BigDecimal minimumBidIncrement;

    /**
     * Whether the increment is a FIXED dollar amount or a PERCENTAGE of the current bid.
     * Defaults to FIXED if not specified.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BidIncrementType bidIncrementType = BidIncrementType.FIXED;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionStatus status;

    @Column(nullable = false)
    private String sellerUsername;

    // Admin moderation fields (from Module 1)
    private String rejectionReason;
    private String cancellationReason;

    @Builder.Default
    private boolean bidsBlocked = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = AuctionStatus.PENDING;
        if (bidIncrementType == null) bidIncrementType = BidIncrementType.FIXED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
