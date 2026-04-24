package com.example.bidoo_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bid_seq")
    @SequenceGenerator(name = "bid_seq", sequenceName = "bid_seq", allocationSize = 20)
    private Long id;

    @ManyToOne(optional = false)
    private AuctionItem auctionItem;

    @ManyToOne(optional = false)
    private User bidder;

    @Column(nullable = false)
    private Double bidAmount;

    private LocalDateTime bidTime;
}