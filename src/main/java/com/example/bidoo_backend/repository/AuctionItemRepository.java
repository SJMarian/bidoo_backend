package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.AuctionItem;
import com.example.bidoo_backend.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {

    List<AuctionItem> getBySeller(User seller);

    @Query(value = """
        SELECT * FROM auction_item a
        WHERE (:keyword IS NULL OR CAST(a.title AS TEXT) ILIKE CONCAT('%', :keyword, '%'))
        AND (:status IS NULL OR a.status = :status)
        AND (:minPrice IS NULL OR a.bid_starting_price >= :minPrice)
        AND (:maxPrice IS NULL OR a.bid_starting_price <= :maxPrice)
        AND (:minBids IS NULL OR a.total_bids >= :minBids)
        AND (:maxBids IS NULL OR a.total_bids <= :maxBids)
        AND (:startDate IS NULL OR DATE(a.start_at) >= CAST(:startDate AS DATE))
        AND (:endDate IS NULL OR DATE(a.end_at) <= CAST(:endDate AS DATE))
        AND (:endingSoon IS NULL OR a.end_at <= :soonTime)
        ORDER BY a.created_at DESC
        """, nativeQuery = true)
    List<AuctionItem> searchAuctions(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("endingSoon") Boolean endingSoon,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("minBids") Integer minBids,
            @Param("maxBids") Integer maxBids,
            @Param("soonTime") LocalDateTime soonTime
    );
}