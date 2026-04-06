package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.AuctionImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionImageRepository extends JpaRepository<AuctionImage, Long> {
    // Additional query methods if needed
}
