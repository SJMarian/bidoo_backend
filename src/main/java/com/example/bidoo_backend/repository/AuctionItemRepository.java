package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.AuctionItem;
import com.example.bidoo_backend.enums.AuctionItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {

    List<AuctionItem> findByStatusAndBidStartingPriceBetween(
            AuctionItemStatus status,
            Double minPrice,
            Double maxPrice
    );
}