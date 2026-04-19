package com.example.bidoo_backend.auction.scheduler;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's @Scheduled annotation processing.
 * Required for the AuctionScheduler to run automatically.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
