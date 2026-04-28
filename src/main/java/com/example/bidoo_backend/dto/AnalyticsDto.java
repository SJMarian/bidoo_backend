package com.example.bidoo_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AnalyticsDto {

    // ── Analytics Dashboard ──────────────────────────────────────────────────

    /** Top-level metrics shown on the admin analytics dashboard */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardMetrics {
        private BigDecimal totalRevenue;
        private long completedAuctions;
        private long unpaidAuctions;
        private long activeAuctions;
        private long totalAuctions;
        private long totalBids;
        private List<TopSellingItem> highestSellingItems;
        private List<CategoryRevenue> revenueByCategory;
    }

    /** One entry in the highest-selling items list */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSellingItem {
        private Long auctionId;
        private String title;
        private String category;
        private BigDecimal finalPrice;
        private String winnerUsername;
    }

    /** Revenue breakdown by category */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRevenue {
        private String category;
        private BigDecimal revenue;
        private long auctionCount;
    }

    // ── Automatic Winner Determination ───────────────────────────────────────

    /** Result of determining winner for a closed auction */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WinnerDeterminationResult {
        private Long auctionId;
        private String auctionTitle;
        private String winnerUsername;
        private BigDecimal winningBidAmount;
        private String auctionStatus;
        private String message;
    }

    // ── Transaction Records & Invoice ─────────────────────────────────────────

    /** Full transaction record for an order */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionRecord {
        private Long transactionId;
        private Long orderId;
        private Long auctionId;
        private String auctionTitle;
        private String winnerUsername;
        private String sellerUsername;
        private BigDecimal paymentAmount;
        private String currency;
        private String paymentStatus;
        private String transactionStatus;
        private String gatewayTrxId;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;
    }

    /** Invoice data — sent to frontend for PDF generation */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceData {
        private String invoiceNumber;
        private Long orderId;
        private Long auctionId;
        private String auctionTitle;
        private String auctionDescription;
        private String buyerName;
        private String buyerEmail;
        private String sellerName;
        private BigDecimal amount;
        private String currency;
        private String paymentStatus;
        private String gatewayTrxId;
        private LocalDateTime issuedAt;
        private LocalDateTime paidAt;
    }

    // ── Bid Growth Trend ─────────────────────────────────────────────────────

    /** One data point in the bid growth chart */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidDataPoint {
        private LocalDateTime placedAt;
        private BigDecimal amount;
        private String bidderUsername;
        private int bidNumber;   // sequential bid number (1, 2, 3...)
    }

    /** Full bid growth trend for one auction */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidGrowthTrend {
        private Long auctionId;
        private String auctionTitle;
        private BigDecimal startingPrice;
        private BigDecimal currentHighestBid;
        private int totalBids;
        private List<BidDataPoint> bidProgression;
        private BigDecimal growthPercent; // how much has the bid grown from starting price
    }
}
