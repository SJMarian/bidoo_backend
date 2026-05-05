package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.Payment;
import com.example.bidoo_backend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    Optional<Payment> findByOrder_Auction_Id(Long auctionId);
}
