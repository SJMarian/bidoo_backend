package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    
    List<UserActivityLog> findByUserIdOrderByTimestampDesc(Long userId);
    
    List<UserActivityLog> findByActivityTypeOrderByTimestampDesc(UserActivityLog.ActivityType activityType);
    
    @Query("SELECT u FROM UserActivityLog u WHERE u.timestamp >= :startTime ORDER BY u.timestamp DESC")
    List<UserActivityLog> findRecentActivity(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT u FROM UserActivityLog u WHERE u.userId = :userId AND u.timestamp >= :startTime ORDER BY u.timestamp DESC")
    List<UserActivityLog> findUserActivitySince(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(DISTINCT u.userId) FROM UserActivityLog u WHERE u.timestamp >= :startTime")
    Long countActiveUsers(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT u.activityType, COUNT(u) as count FROM UserActivityLog u WHERE u.timestamp >= :startTime GROUP BY u.activityType ORDER BY count DESC")
    List<Object[]> getActivityTypeDistribution(@Param("startTime") LocalDateTime startTime);
}
