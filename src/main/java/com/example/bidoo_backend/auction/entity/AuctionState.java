package com.example.bidoo_backend.auction.entity;

public enum AuctionState {
    UPCOMING,   // Not yet started
    ACTIVE,     // Currently accepting bids
    CLOSED,     // End time reached, winner determined
    PAID        // Winner has completed payment
}
