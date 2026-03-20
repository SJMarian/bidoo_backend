package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByAuctionItemId(Long auctionItemId);
}
