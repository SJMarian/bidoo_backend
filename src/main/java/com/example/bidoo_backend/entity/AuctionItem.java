package com.example.bidoo_backend.entity;

import com.example.bidoo_backend.enums.AuctionItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "auction_item")
public class AuctionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ⭐ THIS FIXES YOUR ERROR
    private Long id;

    private String title;
    private String description;
    private String currency;

    private Double bidStartingPrice;
    private Double minimumBidIncrement;
    private Double currentHighestBid;
    private Integer totalBids;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer extendSeconds;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @Enumerated(EnumType.STRING)
    private AuctionItemStatus status;
}