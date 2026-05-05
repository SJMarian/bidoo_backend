package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStatus(AuctionStatus status);
    List<Auction> findByStatusIn(List<AuctionStatus> statuses);
    List<Auction> findBySeller(User seller);

    @Query("SELECT a FROM Auction a WHERE " +
           "(:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:currency IS NULL OR a.currency = :currency)")
    List<Auction> searchAuctions(
        @Param("title") String title,
        @Param("status") AuctionStatus status,
        @Param("currency") String currency
    );
}
