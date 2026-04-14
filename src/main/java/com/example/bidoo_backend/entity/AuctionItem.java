package com.example.bidoo_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.example.bidoo_backend.enums.AuctionItemStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_item_seq")
    @SequenceGenerator(name = "auction_item_seq", sequenceName = "auction_item_seq", allocationSize = 20)
    private Long id;

    private String title;

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

    @Enumerated(EnumType.STRING)
    private AuctionItemStatus status;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Integer extendSeconds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Version
    private Long version;

}