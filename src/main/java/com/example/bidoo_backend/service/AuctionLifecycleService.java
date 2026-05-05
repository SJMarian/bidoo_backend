package com.example.bidoo_backend.service;

import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionLifecycleService {

    private final AuctionRepository auctionRepository;

    @Scheduled(fixedRate = 30000)
    public void updateAuctionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<Auction> auctions = auctionRepository.findAll();

        for (Auction auction : auctions) {
            // Skip terminal statuses
            if (auction.getStatus() == AuctionStatus.PAID ||
                auction.getStatus() == AuctionStatus.CANCELLED ||
                auction.getStatus() == AuctionStatus.REJECTED) {
                continue;
            }

            // Never auto-transition auctions awaiting moderation
            if (auction.getStatus() == AuctionStatus.PENDING) {
                continue;
            }

            if (auction.getStartAt() != null && auction.getEndAt() != null) {
                if (now.isBefore(auction.getStartAt())) {
                    // Only keep as UPCOMING if already approved/upcoming
                    if (auction.getStatus() == AuctionStatus.APPROVED || auction.getStatus() == AuctionStatus.UPCOMING) {
                        auction.setStatus(AuctionStatus.UPCOMING);
                    }
                } else if (!now.isBefore(auction.getStartAt()) && now.isBefore(auction.getEndAt())) {
                    // Only activate if it was approved/upcoming (not pending)
                    if (auction.getStatus() == AuctionStatus.UPCOMING || auction.getStatus() == AuctionStatus.APPROVED) {
                        auction.setStatus(AuctionStatus.ACTIVE);
                    }
                } else if (!now.isBefore(auction.getEndAt())) {
                    if (auction.getStatus() == AuctionStatus.ACTIVE) {
                        auction.setStatus(AuctionStatus.CLOSED);
                    }
                }

                auction.setUpdatedAt(now);
                auctionRepository.save(auction);
            }
        }
    }
}
