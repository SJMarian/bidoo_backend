package com.example.bidoo_backend.service;

import com.example.bidoo_backend.entity.UserActivityLog;
import com.example.bidoo_backend.repository.UserActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserActivityTrackingService {
    
    private final UserActivityLogRepository activityLogRepository;
    
    /**
     * Log user activity
     */
    public void logActivity(Long userId, String username, UserActivityLog.ActivityType activityType,
                           String description, String ipAddress, String userAgent) {
        try {
            UserActivityLog log = new UserActivityLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setActivityType(activityType);
            log.setDescription(description);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setTimestamp(LocalDateTime.now());
            
            activityLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log user activity: {}", e.getMessage());
        }
    }
    
    /**
     * Log activity with related resources
     */
    public void logActivityWithRelations(Long userId, String username, UserActivityLog.ActivityType activityType,
                                        String description, String ipAddress, String userAgent,
                                        Long auctionId, Long bidId, Long orderId) {
        try {
            UserActivityLog log = new UserActivityLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setActivityType(activityType);
            log.setDescription(description);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setRelatedAuctionId(auctionId);
            log.setRelatedBidId(bidId);
            log.setRelatedOrderId(orderId);
            log.setTimestamp(LocalDateTime.now());
            
            activityLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log user activity with relations: {}", e.getMessage());
        }
    }
    
    /**
     * Get user activity history
     */
    public List<UserActivityLog> getUserActivityHistory(Long userId) {
        return activityLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }
    
    /**
     * Get recent activity (last 24 hours by default)
     */
    public List<UserActivityLog> getRecentActivity(int hoursBack) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hoursBack);
        return activityLogRepository.findRecentActivity(startTime);
    }
    
    /**
     * Get user's activity since a specific time
     */
    public List<UserActivityLog> getUserActivitySince(Long userId, LocalDateTime startTime) {
        return activityLogRepository.findUserActivitySince(userId, startTime);
    }
    
    /**
     * Count active users in the last N hours
     */
    public Long countActiveUsers(int hoursBack) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hoursBack);
        return activityLogRepository.countActiveUsers(startTime);
    }
    
    /**
     * Get activity type distribution for last N hours
     */
    public List<Object[]> getActivityTypeDistribution(int hoursBack) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hoursBack);
        return activityLogRepository.getActivityTypeDistribution(startTime);
    }
    
    /**
     * Get activity statistics
     */
    public ActivityStatistics getActivityStatistics(int hoursBack) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hoursBack);
        
        Long activeUsers = activityLogRepository.countActiveUsers(startTime);
        List<UserActivityLog> recentActivity = activityLogRepository.findRecentActivity(startTime);
        
        long bidPlaced = recentActivity.stream()
                .filter(a -> a.getActivityType() == UserActivityLog.ActivityType.BID_PLACED)
                .count();
        
        long paymentCompleted = recentActivity.stream()
                .filter(a -> a.getActivityType() == UserActivityLog.ActivityType.PAYMENT_COMPLETED)
                .count();
        
        return ActivityStatistics.builder()
                .activeUsers(activeUsers)
                .totalActivities((long) recentActivity.size())
                .bidsPlaced(bidPlaced)
                .paymentsCompleted(paymentCompleted)
                .timeWindowHours(hoursBack)
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ActivityStatistics {
        private Long activeUsers;
        private Long totalActivities;
        private Long bidsPlaced;
        private Long paymentsCompleted;
        private Integer timeWindowHours;
    }
}
