package com.example.bidoo_backend.dto;

import com.example.bidoo_backend.enums.BidIncrementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidDto {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PlaceBidRequest {
        private String bidderUsername;
        private BigDecimal amount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BidResponse {
        private Long id;
        private Long auctionId;
        private String bidderUsername;
        private BigDecimal amount;
        private LocalDateTime placedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BidStateResponse {
        private Long auctionId;
        private BigDecimal currentHighestBid;
        private BigDecimal startingPrice;
        private BigDecimal minimumNextBid;
        private BigDecimal minimumBidIncrement;
        private BidIncrementType incrementType;
        private boolean bidsBlocked;
        private String auctionStatus;
        private int totalBids;
        // ── Countdown fields (new) ──
        private LocalDateTime endAt;       // auction end time (server time)
        private LocalDateTime serverTime;  // current server time for sync
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UpdateIncrementRuleRequest {
        private BigDecimal minimumBidIncrement;
        private BidIncrementType incrementType;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BidValidationResponse {
        private boolean valid;
        private String message;
        private BigDecimal minimumNextBid;
    }
}
