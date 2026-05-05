package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.Order;
import com.example.bidoo_backend.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByAuctionId(Long auctionId);

    List<Order> findByStatus(OrderStatus status);

    // Total revenue from all PAID orders
    @Query("SELECT COALESCE(SUM(o.finalPrice), 0) FROM Order o WHERE o.status = 'PAID'")
    BigDecimal sumPaidRevenue();

    // Count of paid orders
    long countByStatus(OrderStatus status);

    // Orders with their auctions for top-selling items
    @Query("SELECT o FROM Order o JOIN FETCH o.auction WHERE o.status = 'PAID' ORDER BY o.finalPrice DESC")
    List<Order> findTopPaidOrdersByRevenue();
}
