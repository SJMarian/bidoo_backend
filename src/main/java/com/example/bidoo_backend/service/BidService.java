package com.example.bidoo_backend.service;

import com.example.bidoo_backend.dto.BidRequest;
import com.example.bidoo_backend.dto.BidResponse;
import com.example.bidoo_backend.entity.AuctionItem;
import com.example.bidoo_backend.entity.Bid;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.enums.AuctionItemStatus;
import com.example.bidoo_backend.repository.AuctionItemRepository;
import com.example.bidoo_backend.repository.BidRepository;
import com.example.bidoo_backend.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public BidResponse placeBid(BidRequest request, Authentication authentication) {
        try {
            User bidder = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AuctionItem auctionItem = auctionItemRepository.findById(request.getAuctionItemId())
                    .orElseThrow(() -> new RuntimeException("Auction item not found"));

            LocalDateTime now = LocalDateTime.now();

            // if (auctionItem.getStatus() != AuctionItemStatus.LIVE) {
            //     throw new RuntimeException("Auction is not live");
            // }

            if (auctionItem.getStartAt() != null && now.isBefore(auctionItem.getStartAt())) {
                throw new RuntimeException("Auction has not started yet");
            }

            if (auctionItem.getEndAt() != null && now.isAfter(auctionItem.getEndAt())) {
                throw new RuntimeException("Auction has already ended");
            }

            Double currentHighestBid = auctionItem.getCurrentHighestBid();

            if (currentHighestBid == null) {
                currentHighestBid = auctionItem.getBidStartingPrice();
            }

            Double minimumRequiredBid = currentHighestBid + auctionItem.getMinimumBidIncrement();

            if (request.getBidAmount() < minimumRequiredBid) {
                throw new RuntimeException("Bid must be at least " + minimumRequiredBid);
            }

            auctionItem.setCurrentHighestBid(request.getBidAmount());
            auctionItem.setCurrentHighestBidder(bidder);
            auctionItem.setTotalBids(
                    auctionItem.getTotalBids() == null ? 1 : auctionItem.getTotalBids() + 1
            );
            auctionItem.setUpdatedAt(now);

            AuctionItem savedAuctionItem = auctionItemRepository.saveAndFlush(auctionItem);

            Bid bid = Bid.builder()
                    .auctionItem(savedAuctionItem)
                    .bidder(bidder)
                    .bidAmount(request.getBidAmount())
                    .bidTime(now)
                    .build();

            Bid savedBid = bidRepository.save(bid);

            return BidResponse.builder()
                    .bidId(savedBid.getId())
                    .auctionItemId(savedAuctionItem.getId())
                    .bidAmount(savedBid.getBidAmount())
                    .bidderEmail(bidder.getEmail())
                    .currentHighestBid(savedAuctionItem.getCurrentHighestBid())
                    .totalBids(savedAuctionItem.getTotalBids())
                    .version(savedAuctionItem.getVersion())
                    .bidTime(savedBid.getBidTime())
                    .build();

        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new RuntimeException("Another user placed a bid at the same time. Please try again.");
        }
    }
}