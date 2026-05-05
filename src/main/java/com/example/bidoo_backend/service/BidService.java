package com.example.bidoo_backend.service;

import com.example.bidoo_backend.dto.BidDto;
import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.entity.Bid;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.repository.AuctionRepository;
import com.example.bidoo_backend.repository.BidRepository;
import com.example.bidoo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final AuctionNotificationService auctionNotificationService;

    /**
     * Place a bid on an auction.
     * Automatically triggers:
     *  - BID_PLACED notification to the bidder
     *  - BID_OUTBID notification to the previous highest bidder
     */
    @Transactional
    public BidDto.BidResponse placeBid(Long auctionId, String bidderUsername, BigDecimal amount) {
        // 1. Load and validate auction
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction is not active. Current status: " + auction.getStatus());
        }
        if (auction.isBidsBlocked()) {
            throw new IllegalStateException("Bidding is currently blocked for this auction.");
        }

        // 2. Validate bid amount >= current highest + minimum increment
        BigDecimal currentHighest = bidRepository.findHighestBidAmount(auctionId)
                .orElse(BigDecimal.valueOf(auction.getBidStartingPrice()));
        BigDecimal minimumRequired = computeMinimumBid(auction, currentHighest);

        if (amount.compareTo(minimumRequired) < 0) {
            throw new IllegalArgumentException(
                    String.format("Bid $%.2f is below the minimum required $%.2f", amount, minimumRequired));
        }

        // 3. Capture the previous highest bidder before saving new bid
        Optional<Bid> previousHighestBid = bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId);

        // 4. Find bidder user by email
        Optional<User> bidderUser = userRepository.findByEmail(bidderUsername);
        Long bidderId = bidderUser.map(User::getId).orElse(null);

        // 5. Save the new bid
        Bid bid = Bid.builder()
                .auctionId(auctionId)
                .bidderUsername(bidderUsername)
                .amount(amount)
                .build();
        Bid saved = bidRepository.save(bid);

        // 6. Update auction stats
        auction.setCurrentHighestBid(amount.doubleValue());
        auction.setTotalBids(auction.getTotalBids() == null ? 1 : auction.getTotalBids() + 1);
        bidderUser.ifPresent(auction::setCurrentHighestBidder);
        auctionRepository.save(auction);

        // 7. Notify bidder — bid placed successfully
        if (bidderId != null) {
            try {
                auctionNotificationService.notifyBidPlaced(
                        bidderId, auction.getTitle(), amount.doubleValue(), auctionId, saved.getId());
            } catch (Exception e) {
                log.warn("BID_PLACED notification failed for user {}: {}", bidderId, e.getMessage());
            }
        }

        // 8. Notify previous highest bidder — outbid
        if (previousHighestBid.isPresent()) {
            String prevBidder = previousHighestBid.get().getBidderUsername();
            if (!prevBidder.equals(bidderUsername)) {
                userRepository.findByEmail(prevBidder).ifPresent(prevUser -> {
                    try {
                        auctionNotificationService.notifyOutbid(
                                prevUser.getId(), auction.getTitle(), amount.doubleValue(), auctionId);
                    } catch (Exception e) {
                        log.warn("BID_OUTBID notification failed for user {}: {}", prevUser.getId(), e.getMessage());
                    }
                });
            }
        }

        log.info("Bid placed: auction={}, bidder={}, amount={}", auctionId, bidderUsername, amount);
        return toResponse(saved);
    }

    /** Get all bids for an auction ordered by amount descending */
    public List<BidDto.BidResponse> getBidsForAuction(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByAmountDesc(auctionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Get bid history ordered by time descending */
    public List<BidDto.BidResponse> getBidHistory(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByPlacedAtDesc(auctionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Get the current highest bid */
    public Optional<BidDto.BidResponse> getHighestBid(Long auctionId) {
        return bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId).map(this::toResponse);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private BigDecimal computeMinimumBid(Auction auction, BigDecimal currentHighest) {
        if (auction.getMinimumBidIncrement() == null) return currentHighest;
        BigDecimal increment = switch (auction.getBidIncrementType()) {
            case PERCENTAGE -> currentHighest.multiply(
                    BigDecimal.valueOf(auction.getMinimumBidIncrement() / 100.0));
            default -> BigDecimal.valueOf(auction.getMinimumBidIncrement());
        };
        return currentHighest.add(increment);
    }

    private BidDto.BidResponse toResponse(Bid bid) {
        return BidDto.BidResponse.builder()
                .id(bid.getId())
                .auctionId(bid.getAuctionId())
                .bidderUsername(bid.getBidderUsername())
                .amount(bid.getAmount())
                .placedAt(bid.getPlacedAt())
                .build();
    }
}
