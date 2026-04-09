package com.example.bidoo_backend.repository;

import com.example.bidoo_backend.entity.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    List<AdminActionLog> findByAuctionIdOrderByPerformedAtDesc(Long auctionId);
    List<AdminActionLog> findAllByOrderByPerformedAtDesc();
}
