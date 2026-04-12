package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.AuctionItem;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.enums.AuctionItemStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {

    List<AuctionItem> getBySeller(User seller);

    @Query("""
        SELECT a FROM AuctionItem a
        WHERE (:status IS NULL OR a.status = :status)
        AND (:minPrice IS NULL OR a.bidStartingPrice >= :minPrice)
        AND (:maxPrice IS NULL OR a.bidStartingPrice <= :maxPrice)
    """)
    List<AuctionItem> searchAuctions(
            @Param("status") AuctionItemStatus status,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );
}