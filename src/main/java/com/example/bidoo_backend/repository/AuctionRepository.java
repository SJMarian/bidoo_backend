package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStatus(AuctionStatus status);
    List<Auction> findByStatusIn(List<AuctionStatus> statuses);
}
