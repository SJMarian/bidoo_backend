package com.example.bidoo_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.example.bidoo_backend.enums.PaymentStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq")
    @SequenceGenerator(name = "payment_seq", sequenceName = "payment_seq", allocationSize = 20)
    private Long id;

    @OneToOne
    private Order order;

    @ManyToOne
    private User user;

    private Double amount;
    private String currency;
    
    // Explicitly named paymentMethod as per standard naming conventions
    private String paymentMethod;
    private String gateway;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Using cascade to save transactions associated with the payment easily
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentTransaction> paymentTransactions;
}
