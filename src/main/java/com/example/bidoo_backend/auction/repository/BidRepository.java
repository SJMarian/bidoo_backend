package com.example.bidoo_backend.auction.repository;

import com.example.bidoo_backend.auction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
}