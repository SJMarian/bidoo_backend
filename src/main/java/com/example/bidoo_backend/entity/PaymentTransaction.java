package com.example.bidoo_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.example.bidoo_backend.enums.TransactionStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_transaction_seq")
    @SequenceGenerator(name = "payment_transaction_seq", sequenceName = "payment_transaction_seq", allocationSize = 20)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private String gatewayTrxId;

    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    private LocalDateTime createdAt;
}