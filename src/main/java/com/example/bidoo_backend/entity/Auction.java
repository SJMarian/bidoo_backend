package com.example.bidoo_backend.entity;

import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.enums.BidIncrementType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Unified Auction entity combining original Auction and AuctionItem schemas.
 * Supports both bidding system and order/payment workflow.
 */
@Entity
@Table(name = "auction_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_item_seq")
    @SequenceGenerator(name = "auction_item_seq", sequenceName = "auction_item_seq", allocationSize = 20)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    private User seller;

    private String currency;

    private Double bidStartingPrice;

    private Double minimumBidIncrement;

    private Double currentHighestBid;

    @ManyToOne
    private User currentHighestBidder;

    private Integer totalBids;

    /**
     * Whether the increment is a FIXED dollar amount or a PERCENTAGE of the current bid.
     * Defaults to FIXED if not specified.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BidIncrementType bidIncrementType = BidIncrementType.FIXED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionStatus status;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    private Integer extendSeconds;

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
