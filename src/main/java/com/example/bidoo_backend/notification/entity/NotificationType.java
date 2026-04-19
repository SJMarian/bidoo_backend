package com.example.bidoo_backend.notification.entity;

public enum NotificationType {
    AUCTION_WON,          // Winner alert: you won, please pay
    AUCTION_CLOSED,       // General closed notification
    OUTBID,               // Someone outbid you
    AUCTION_STARTING,     // An auction you watched is starting
    BID_PLACED,           // Your bid was placed successfully
    PAYMENT_REQUIRED,     // Reminder to complete payment
    AUCTION_APPROVED,     // Admin approved your submitted auction
    AUCTION_REJECTED      // Admin rejected your submitted auction
}
