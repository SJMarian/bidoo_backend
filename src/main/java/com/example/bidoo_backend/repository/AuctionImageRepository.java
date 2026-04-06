package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.AuctionImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionImageRepository extends JpaRepository<AuctionImage, Long> {
}
