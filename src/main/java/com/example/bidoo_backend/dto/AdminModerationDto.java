package com.example.bidoo_backend.dto;

import com.example.bidoo_backend.enums.AdminActionType;
import com.example.bidoo_backend.enums.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminModerationDto {

    /** Response DTO representing an auction for admin review */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuctionAdminResponse {
        private Long id;
        private String title;
        private String category;
        private String description;
        private BigDecimal startingPrice;
        private BigDecimal minimumBidIncrement;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private AuctionStatus status;
        private String sellerUsername;
        private String rejectionReason;
        private String cancellationReason;
        private boolean bidsBlocked;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /** Request DTO for approve action */
    @Data
    @NoArgsConstructor
    //@AllArgsConstructor
    public static class ApproveRequest {
        // No extra fields needed; just the auctionId in path
    }

    /** Request DTO for reject action */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectRequest {
        private String reason;
    }

    /** Request DTO for cancel action */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelRequest {
        private String reason;
    }

    /** Request DTO for manual close action */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManualCloseRequest {
        private String reason;
    }

    /** Request DTO for reopen action */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReopenRequest {
        private LocalDateTime newEndTime;
        private String reason;
    }

    /** Response DTO for admin action log entries */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminActionLogResponse {
        private Long id;
        private Long auctionId;
        private String auctionTitle;
        private AdminActionType actionType;
        private String reason;
        private String performedBy;
        private LocalDateTime performedAt;
    }
}
