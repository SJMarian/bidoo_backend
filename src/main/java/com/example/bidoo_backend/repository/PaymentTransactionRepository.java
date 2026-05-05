package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByGatewayTrxId(String gatewayTrxId);

    List<PaymentTransaction> findByPaymentId(Long paymentId);

    // Latest transaction for a payment
    Optional<PaymentTransaction> findTopByPaymentIdOrderByCreatedAtDesc(Long paymentId);
}
