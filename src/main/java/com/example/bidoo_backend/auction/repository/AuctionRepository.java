package com.example.bidoo_backend.auction.repository;

import com.example.bidoo_backend.auction.entity.Auction;
import com.example.bidoo_backend.auction.entity.AuctionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    // Find all UPCOMING auctions whose start time has passed — should become ACTIVE
    @Query("SELECT a FROM Auction a WHERE a.state = :state AND a.startTime <= :now")
    List<Auction> findAuctionsToActivate(@Param("now") LocalDateTime now, @Param("state") AuctionState state);

    // Find all ACTIVE auctions whose end time has passed — should become CLOSED
    @Query("SELECT a FROM Auction a WHERE a.state = :state AND a.endTime <= :now")
    List<Auction> findAuctionsToClose(@Param("now") LocalDateTime now, @Param("state") AuctionState state);

    // Find all auctions in a given state
    List<Auction> findByState(AuctionState state);
}
