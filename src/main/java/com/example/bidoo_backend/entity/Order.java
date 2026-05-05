package com.example.bidoo_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.example.bidoo_backend.enums.OrderStatus;

@Entity
@Table(name = "orders") // "order" is a reserved keyword in SQL, so we use "orders"
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "order_seq", allocationSize = 20)
    private Long id;

    @OneToOne
    private Auction auction;

    @ManyToOne
    private User buyer;

    @ManyToOne
    private User seller;

    private Double finalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;
}