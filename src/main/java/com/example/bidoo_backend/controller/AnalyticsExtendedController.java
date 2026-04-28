package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.service.UserActivityTrackingService;
import com.example.bidoo_backend.service.AuctionPerformanceAnalyticsService;
import com.example.bidoo_backend.entity.UserActivityLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsExtendedController {
    
    private final UserActivityTrackingService activityTrackingService;
    private final AuctionPerformanceAnalyticsService auctionPerformanceService;
    
    // ── User Activity Tracking ──
    
    @GetMapping("/activity/user/{userId}")
    public ResponseEntity<List<UserActivityLog>> getUserActivity(@PathVariable Long userId) {
        return ResponseEntity.ok(activityTrackingService.getUserActivityHistory(userId));
    }
    
    @GetMapping("/activity/recent")
    public ResponseEntity<List<UserActivityLog>> getRecentActivity(
            @RequestParam(defaultValue = "24") int hoursBack) {
        return ResponseEntity.ok(activityTrackingService.getRecentActivity(hoursBack));
    }
    
    @GetMapping("/activity/statistics")
    public ResponseEntity<UserActivityTrackingService.ActivityStatistics> getActivityStatistics(
            @RequestParam(defaultValue = "24") int hoursBack) {
        return ResponseEntity.ok(activityTrackingService.getActivityStatistics(hoursBack));
    }
    
    @GetMapping("/activity/active-users")
    public ResponseEntity<Long> getActiveUsersCount(
            @RequestParam(defaultValue = "24") int hoursBack) {
        return ResponseEntity.ok(activityTrackingService.countActiveUsers(hoursBack));
    }
    
    @GetMapping("/activity/type-distribution")
    public ResponseEntity<List<Object[]>> getActivityTypeDistribution(
            @RequestParam(defaultValue = "24") int hoursBack) {
        return ResponseEntity.ok(activityTrackingService.getActivityTypeDistribution(hoursBack));
    }
    
    // ── Auction Performance Analytics ──
    
    @GetMapping("/performance/auction/{auctionId}")
    public ResponseEntity<AuctionPerformanceAnalyticsService.AuctionPerformance> 
            getAuctionPerformance(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionPerformanceService.getAuctionPerformance(auctionId));
    }
    
    @GetMapping("/performance/all-auctions")
    public ResponseEntity<List<AuctionPerformanceAnalyticsService.AuctionPerformance>> 
            getAllAuctionsPerformance() {
        return ResponseEntity.ok(auctionPerformanceService.getAllAuctionsPerformance());
    }
    
    @GetMapping("/performance/top-auctions")
    public ResponseEntity<List<AuctionPerformanceAnalyticsService.AuctionPerformance>> 
            getTopAuctions(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(auctionPerformanceService.getTopPerformingAuctions(limit));
    }
    
    @GetMapping("/performance/by-category")
    public ResponseEntity<Map<String, AuctionPerformanceAnalyticsService.CategoryPerformance>> 
            getPerformanceByCategory() {
        return ResponseEntity.ok(auctionPerformanceService.getPerformanceByCategory());
    }
}
