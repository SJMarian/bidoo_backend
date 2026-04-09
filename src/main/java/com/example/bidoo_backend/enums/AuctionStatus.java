package com.example.bidoo_backend.enums;

public enum AuctionStatus {
    PENDING,      // Submitted by user, awaiting admin review
    APPROVED,     // Approved by admin (becomes UPCOMING when start time arrives)
    UPCOMING,     // Active & visible, not yet started
    ACTIVE,       // Currently accepting bids
    CLOSED,       // End time reached, winner determined
    PAID,         // Payment completed
    REJECTED,     // Rejected by admin
    CANCELLED     // Cancelled by admin after approval
}
