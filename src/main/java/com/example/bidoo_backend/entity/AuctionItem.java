package com.example.bidoo_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.example.bidoo_backend.enums.AuctionItemStatus;

@Entity
@Table(name = "auction_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_item_seq")
    @SequenceGenerator(
            name = "auction_item_seq",
            sequenceName = "auction_item_seq",
            allocationSize = 20
    )
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    private String currency;

    @Column(name = "bid_starting_price")
    private Double bidStartingPrice;

    @Column(name = "minimum_bid_increment")
    private Double minimumBidIncrement;

    @Column(name = "current_highest_bid")
    private Double currentHighestBid;

    @ManyToOne
    @JoinColumn(name = "current_highest_bidder_id")
    private User currentHighestBidder;

    @Column(name = "total_bids")
    private Integer totalBids;

    @Enumerated(EnumType.STRING)
    private AuctionItemStatus status;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "extend_seconds")
    private Integer extendSeconds;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}