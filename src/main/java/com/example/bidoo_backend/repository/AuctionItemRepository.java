package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionItemRepository extends JpaRepository<Auction, Long> {
      List<Auction> getBySeller(User seller);
}
