package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.AuctionItem;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.enums.AuctionItemStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {
    List<AuctionItem> getBySeller(User seller);

    List<AuctionItem> findBySellerNot(User seller);

    List<AuctionItem> findByStatus(AuctionItemStatus status);

    List<AuctionItem> findByStatusAndCurrentHighestBidBetween(
            AuctionItemStatus status,
            Double minPrice,
            Double maxPrice
    );

    List<AuctionItem> findByCurrentHighestBidBetween(
            Double minPrice,
            Double maxPrice
    );

    List<AuctionItem> findByEndAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<AuctionItem> findByStartAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<AuctionItem> findByTotalBidsGreaterThanEqual(Integer totalBids);
}
