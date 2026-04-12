package com.example.bidoo_backend.dto;

import com.example.bidoo_backend.enums.BidIncrementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidDto {

    /** Request to place a bid */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceBidRequest {
        private String bidderUsername;
        private BigDecimal amount;
    }

    /** Response after placing or viewing a bid */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidResponse {
        private Long id;
        private Long auctionId;
        private String bidderUsername;
        private BigDecimal amount;
        private LocalDateTime placedAt;
    }

    /**
     * Full bid state response — what the frontend needs to:
     * - Show current highest bid
     * - Compute the minimum next valid bid
     * - Enforce the increment rule on the frontend too
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidStateResponse {
        private Long auctionId;
        private BigDecimal currentHighestBid;   // null if no bids yet
        private BigDecimal startingPrice;
        private BigDecimal minimumNextBid;       // pre-computed by backend
        private BigDecimal minimumBidIncrement;
        private BidIncrementType incrementType;
        private boolean bidsBlocked;
        private String auctionStatus;
        private int totalBids;
    }

    /** Request to update the bid increment rule for an auction (admin) */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateIncrementRuleRequest {
        private BigDecimal minimumBidIncrement;
        private BidIncrementType incrementType;
    }

    /** Validation result returned before placing a bid */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidValidationResponse {
        private boolean valid;
        private String message;
        private BigDecimal minimumNextBid;
    }
}
