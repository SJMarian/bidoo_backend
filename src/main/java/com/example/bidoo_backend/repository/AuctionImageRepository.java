package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.AuctionImage;
import com.example.bidoo_backend.entity.AuctionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionImageRepository extends JpaRepository<AuctionImage, Long> {
    List<AuctionImage> findByAuctionItem(AuctionItem auctionItem);
}
