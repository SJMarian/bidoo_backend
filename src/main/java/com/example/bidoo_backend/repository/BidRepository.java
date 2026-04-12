package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionIdOrderByAmountDesc(Long auctionId);

    @Query("SELECT MAX(b.amount) FROM Bid b WHERE b.auctionId = :auctionId")
    Optional<BigDecimal> findHighestBidAmount(Long auctionId);

    Optional<Bid> findTopByAuctionIdOrderByAmountDesc(Long auctionId);

    List<Bid> findByAuctionIdOrderByPlacedAtDesc(Long auctionId);
}
