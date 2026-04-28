package com.example.bidoo_backend.service;

import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.entity.Bid;
import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.repository.AuctionRepository;
import com.example.bidoo_backend.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionPerformanceAnalyticsService {
    
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    
    /**
     * Get performance metrics for a specific auction
     */
    public AuctionPerformance getAuctionPerformance(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
        
        List<Bid> bids = bidRepository.findByAuctionIdOrderByPlacedAtAsc(auctionId);
        
        return calculateAuctionPerformance(auction, bids);
    }
    
    /**
     * Get performance metrics for all auctions
     */
    public List<AuctionPerformance> getAllAuctionsPerformance() {
        return auctionRepository.findAll().stream()
                .map(auction -> {
                    List<Bid> bids = bidRepository.findByAuctionIdOrderByPlacedAtAsc(auction.getId());
                    return calculateAuctionPerformance(auction, bids);
                })
                .sorted(Comparator.comparing(AuctionPerformance::getEngagementScore).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Get top performing auctions
     */
    public List<AuctionPerformance> getTopPerformingAuctions(int limit) {
        return getAllAuctionsPerformance().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get performance by category
     */
    public Map<String, CategoryPerformance> getPerformanceByCategory() {
        Map<String, CategoryPerformance> categoryStats = new LinkedHashMap<>();
        
        auctionRepository.findAll().forEach(auction -> {
            String category = auction.getCategory() != null ? auction.getCategory() : "Other";
            
            List<Bid> bids = bidRepository.findByAuctionIdOrderByPlacedAtAsc(auction.getId());
            AuctionPerformance perf = calculateAuctionPerformance(auction, bids);
            
            categoryStats.computeIfAbsent(category, k -> new CategoryPerformance(category))
                    .addAuction(perf);
        });
        
        return categoryStats;
    }
    
    private AuctionPerformance calculateAuctionPerformance(Auction auction, List<Bid> bids) {
        int bidCount = bids.size();
        double startPrice = auction.getBidStartingPrice() != null ? auction.getBidStartingPrice() : 0.0;
        double finalPrice = auction.getCurrentHighestBid() != null ? auction.getCurrentHighestBid() : startPrice;
        
        double priceGrowth = startPrice > 0 ? ((finalPrice - startPrice) / startPrice) * 100 : 0;
        double engagementScore = calculateEngagementScore(bidCount, priceGrowth);
        
        return AuctionPerformance.builder()
                .auctionId(auction.getId())
                .title(auction.getTitle())
                .category(auction.getCategory())
                .status(auction.getStatus().name())
                .startingPrice(startPrice)
                .finalPrice(finalPrice)
                .priceGrowthPercent(priceGrowth)
                .bidCount(bidCount)
                .engagementScore(engagementScore)
                .build();
    }
    
    private double calculateEngagementScore(int bidCount, double priceGrowth) {
        // Score = (bid_count * 10) + (price_growth * 2)
        // Higher bids and price growth = higher score
        return (bidCount * 10.0) + (Math.max(0, Math.min(priceGrowth, 100)) * 2.0);
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuctionPerformance {
        private Long auctionId;
        private String title;
        private String category;
        private String status;
        private Double startingPrice;
        private Double finalPrice;
        private Double priceGrowthPercent;
        private Integer bidCount;
        private Double engagementScore;
    }
    
    @lombok.Data
    public static class CategoryPerformance {
        private String category;
        private List<AuctionPerformance> auctions = new ArrayList<>();
        private Integer totalAuctions;
        private Integer totalBids;
        private Double averagePriceGrowth;
        private Double averageEngagementScore;
        
        public CategoryPerformance(String category) {
            this.category = category;
        }
        
        public void addAuction(AuctionPerformance auction) {
            auctions.add(auction);
            recalculateStats();
        }
        
        private void recalculateStats() {
            this.totalAuctions = auctions.size();
            this.totalBids = (int) auctions.stream()
                    .mapToLong(a -> a.bidCount != null ? a.bidCount : 0)
                    .sum();
            this.averagePriceGrowth = auctions.isEmpty() ? 0.0 :
                    auctions.stream()
                            .mapToDouble(a -> a.priceGrowthPercent != null ? a.priceGrowthPercent : 0)
                            .average()
                            .orElse(0.0);
            this.averageEngagementScore = auctions.isEmpty() ? 0.0 :
                    auctions.stream()
                            .mapToDouble(a -> a.engagementScore != null ? a.engagementScore : 0)
                            .average()
                            .orElse(0.0);
        }
    }
}
