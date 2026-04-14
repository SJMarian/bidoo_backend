package com.example.bidoo_backend.service;

import com.example.bidoo_backend.dto.BidDto;
import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.entity.Bid;
import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.enums.BidIncrementType;
import com.example.bidoo_backend.repository.AuctionRepository;
import com.example.bidoo_backend.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidIncrementService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    // ── Rule Engine Core ─────────────────────────────────────────────────────

    /**
     * Computes the minimum valid next bid for an auction.
     * Supports both FIXED and PERCENTAGE increment types.
     *
     * FIXED:      minimumNextBid = currentHighest + fixedIncrement
     * PERCENTAGE: minimumNextBid = currentHighest + (currentHighest * percentage / 100)
     */
    public BigDecimal computeMinimumNextBid(Auction auction) {
        Optional<BigDecimal> highestBidOpt = bidRepository.findHighestBidAmount(auction.getId());

        BigDecimal base = highestBidOpt.orElse(
            auction.getBidStartingPrice() != null ? BigDecimal.valueOf(auction.getBidStartingPrice()) : BigDecimal.ZERO
        );
        BigDecimal increment = BigDecimal.valueOf(
            auction.getMinimumBidIncrement() != null ? auction.getMinimumBidIncrement() : 0.0
        );
        BidIncrementType type = auction.getBidIncrementType();

        if (type == BidIncrementType.PERCENTAGE) {
            // e.g. base=100, increment=5 → nextMin = 100 + (100*5/100) = 105
            BigDecimal percentageAmount = base
                    .multiply(increment)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return base.add(percentageAmount).setScale(2, RoundingMode.HALF_UP);
        } else {
            // FIXED: base + increment
            return base.add(increment).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Validates whether a proposed bid amount is acceptable.
     */
    public BidDto.BidValidationResponse validateBid(Auction auction, BigDecimal proposedAmount) {
        // Check auction is active
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            return BidDto.BidValidationResponse.builder()
                    .valid(false)
                    .message("Auction is not currently active. Status: " + auction.getStatus())
                    .minimumNextBid(computeMinimumNextBid(auction))
                    .build();
        }

        // Check bids not blocked
        if (auction.isBidsBlocked()) {
            return BidDto.BidValidationResponse.builder()
                    .valid(false)
                    .message("Bidding is currently blocked for this auction.")
                    .minimumNextBid(computeMinimumNextBid(auction))
                    .build();
        }

        BigDecimal minimumNextBid = computeMinimumNextBid(auction);

        // Check bid meets minimum
        if (proposedAmount.compareTo(minimumNextBid) < 0) {
            return BidDto.BidValidationResponse.builder()
                    .valid(false)
                    .message(String.format(
                            "Bid amount $%.2f is too low. Minimum required bid is $%.2f.",
                            proposedAmount, minimumNextBid))
                    .minimumNextBid(minimumNextBid)
                    .build();
        }

        return BidDto.BidValidationResponse.builder()
                .valid(true)
                .message("Bid is valid.")
                .minimumNextBid(minimumNextBid)
                .build();
    }

    // ── Place Bid ────────────────────────────────────────────────────────────

    @Transactional
    public BidDto.BidResponse placeBid(Long auctionId, BidDto.PlaceBidRequest request) {
        Auction auction = findAuctionOrThrow(auctionId);

        BidDto.BidValidationResponse validation = validateBid(auction, request.getAmount());
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        Bid bid = Bid.builder()
                .auctionId(auctionId)
                .bidderUsername(request.getBidderUsername())
                .amount(request.getAmount())
                .build();

        Bid saved = bidRepository.save(bid);
        return toBidResponse(saved);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public BidDto.BidStateResponse getBidState(Long auctionId) {
        Auction auction = findAuctionOrThrow(auctionId);

        Optional<BigDecimal> highest = bidRepository.findHighestBidAmount(auctionId);
        int totalBids = bidRepository.findByAuctionIdOrderByAmountDesc(auctionId).size();

        return BidDto.BidStateResponse.builder()
                .auctionId(auctionId)
                .currentHighestBid(highest.orElse(null))
                .startingPrice(auction.getBidStartingPrice() != null ? java.math.BigDecimal.valueOf(auction.getBidStartingPrice()) : java.math.BigDecimal.ZERO)
                .minimumNextBid(computeMinimumNextBid(auction))
                .minimumBidIncrement(auction.getMinimumBidIncrement() != null ? java.math.BigDecimal.valueOf(auction.getMinimumBidIncrement()) : java.math.BigDecimal.ZERO)
                .incrementType(auction.getBidIncrementType())
                .bidsBlocked(auction.isBidsBlocked())
                .auctionStatus(auction.getStatus().name())
                .totalBids(totalBids)
                .build();
    }

    public List<BidDto.BidResponse> getBidHistory(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByPlacedAtDesc(auctionId)
                .stream()
                .map(this::toBidResponse)
                .collect(Collectors.toList());
    }

    public BidDto.BidValidationResponse prevalidateBid(Long auctionId, BigDecimal amount) {
        Auction auction = findAuctionOrThrow(auctionId);
        return validateBid(auction, amount);
    }

    // ── Admin: Update Increment Rule ─────────────────────────────────────────

    @Transactional
    public BidDto.BidStateResponse updateIncrementRule(
            Long auctionId, BidDto.UpdateIncrementRuleRequest request) {
        Auction auction = findAuctionOrThrow(auctionId);

        if (request.getMinimumBidIncrement() != null) {
            auction.setMinimumBidIncrement(request.getMinimumBidIncrement().doubleValue());
        }
        if (request.getIncrementType() != null) {
            auction.setBidIncrementType(request.getIncrementType());
        }

        auctionRepository.save(auction);
        return getBidState(auctionId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Auction findAuctionOrThrow(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + id));
    }

    private BidDto.BidResponse toBidResponse(Bid bid) {
        return BidDto.BidResponse.builder()
                .id(bid.getId())
                .auctionId(bid.getAuctionId())
                .bidderUsername(bid.getBidderUsername())
                .amount(bid.getAmount())
                .placedAt(bid.getPlacedAt())
                .build();
    }
}
