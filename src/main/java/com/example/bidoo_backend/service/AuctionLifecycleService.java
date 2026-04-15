package com.example.bidoo_backend.service;

import com.example.bidoo_backend.entity.AuctionItem;
import com.example.bidoo_backend.enums.AuctionItemStatus;
import com.example.bidoo_backend.repository.AuctionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionLifecycleService {

    private final AuctionItemRepository auctionItemRepository;

    @Scheduled(fixedRate = 5000)
    public void updateAuctionStatuses() {

        List<AuctionItem> items = auctionItemRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (AuctionItem item : items) {

            if (item.getStartAt() == null || item.getEndAt() == null) {
                continue;
            }

            if (item.getStatus() == AuctionItemStatus.PAID ||
                item.getStatus() == AuctionItemStatus.CANCELLED) {
                continue;
            }

            if (now.isBefore(item.getStartAt())) {
                item.setStatus(AuctionItemStatus.PENDING);
            } else if (now.isBefore(item.getEndAt())) {
                item.setStatus(AuctionItemStatus.LIVE);
            } else {
                item.setStatus(AuctionItemStatus.ENDED);
            }
        }

        auctionItemRepository.saveAll(items);
    }
}