package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.AuctionImage;
import com.example.bidoo_backend.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionImageRepository extends JpaRepository<AuctionImage, Long> {
    List<AuctionImage> findByAuction(Auction auction);
    Optional<AuctionImage> findFirstByAuction(Auction auction);
}
