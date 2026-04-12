package com.example.bidoo_backend.auction.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Auction entity.
 * If your project already has an Auction entity, do NOT create a new one.
 * Instead, make sure it has at minimum the fields marked with // REQUIRED below,
 * and copy over only the missing ones.
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

    @Column(length = 2000)
    private String description;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "starting_price", nullable = false)
    private double startingPrice;

    @Column(name = "current_highest_bid")
    private double currentHighestBid;

    @Column(name = "highest_bidder_id")
    private Long highestBidderId;

    // REQUIRED — countdown reads these two fields
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // REQUIRED — scheduler transitions this field
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuctionState state = AuctionState.UPCOMING;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
