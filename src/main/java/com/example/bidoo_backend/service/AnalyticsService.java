package com.example.bidoo_backend.service;

import com.example.bidoo_backend.dto.AnalyticsDto;
import com.example.bidoo_backend.entity.Auction;
import com.example.bidoo_backend.entity.Bid;
import com.example.bidoo_backend.entity.Order;
import com.example.bidoo_backend.entity.Payment;
import com.example.bidoo_backend.entity.PaymentTransaction;
import com.example.bidoo_backend.enums.AuctionStatus;
import com.example.bidoo_backend.enums.OrderStatus;
import com.example.bidoo_backend.enums.PaymentStatus;
import com.example.bidoo_backend.repository.AuctionRepository;
import com.example.bidoo_backend.repository.BidRepository;
import com.example.bidoo_backend.repository.OrderRepository;
import com.example.bidoo_backend.repository.PaymentRepository;
import com.example.bidoo_backend.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    // ── 1. Analytics Dashboard ────────────────────────────────────────────────

    public AnalyticsDto.DashboardMetrics getDashboardMetrics() {
        List<Auction> allAuctions = auctionRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();
        List<Bid> allBids = bidRepository.findAll();

        // Revenue from PAID orders
        BigDecimal totalRevenue = orderRepository.sumPaidRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long completedAuctions = allAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.PAID).count();

        long unpaidAuctions = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING).count();

        long activeAuctions = allAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.ACTIVE).count();

        // Top 5 highest-selling paid items
        List<AnalyticsDto.TopSellingItem> topItems = orderRepository.findTopPaidOrdersByRevenue()
                .stream()
                .limit(5)
                .map(o -> AnalyticsDto.TopSellingItem.builder()
                        .auctionId(o.getAuction() != null ? o.getAuction().getId() : null)
                        .title(o.getAuction() != null ? o.getAuction().getTitle() : "Unknown")
                        .category(o.getAuction() != null ? o.getAuction().getCategory() : "Unknown")
                        .finalPrice(o.getFinalPrice() != null
                                ? BigDecimal.valueOf(o.getFinalPrice()) : BigDecimal.ZERO)
                        .winnerUsername(o.getBuyer() != null ? o.getBuyer().getName() : "Unknown")
                        .build())
                .collect(Collectors.toList());

        // Revenue by category from PAID orders
        Map<String, BigDecimal> categoryRevMap = new LinkedHashMap<>();
        Map<String, Long> categoryCntMap = new LinkedHashMap<>();
        for (Order o : allOrders) {
            if (o.getStatus() == OrderStatus.PAID && o.getAuction() != null) {
                String cat = o.getAuction().getCategory() != null
                        ? o.getAuction().getCategory() : "Other";
                categoryRevMap.merge(cat,
                        BigDecimal.valueOf(o.getFinalPrice() != null ? o.getFinalPrice() : 0.0),
                        BigDecimal::add);
                categoryCntMap.merge(cat, 1L, Long::sum);
            }
        }

        List<AnalyticsDto.CategoryRevenue> categoryRevenue = categoryRevMap.entrySet().stream()
                .map(e -> AnalyticsDto.CategoryRevenue.builder()
                        .category(e.getKey())
                        .revenue(e.getValue())
                        .auctionCount(categoryCntMap.getOrDefault(e.getKey(), 0L))
                        .build())
                .sorted(Comparator.comparing(AnalyticsDto.CategoryRevenue::getRevenue).reversed())
                .collect(Collectors.toList());

        return AnalyticsDto.DashboardMetrics.builder()
                .totalRevenue(totalRevenue)
                .completedAuctions(completedAuctions)
                .unpaidAuctions(unpaidAuctions)
                .activeAuctions(activeAuctions)
                .totalAuctions(allAuctions.size())
                .totalBids(allBids.size())
                .highestSellingItems(topItems)
                .revenueByCategory(categoryRevenue)
                .build();
    }

    // ── 2. Automatic Winner Determination ─────────────────────────────────────

    /**
     * Determines the winner of a CLOSED auction:
     * - Finds the highest valid bid
     * - Sets currentHighestBidder on auction
     * - Updates auction status to CLOSED (winner determined)
     * - Returns the result with winner info
     */
    @Transactional
    public AnalyticsDto.WinnerDeterminationResult determineWinner(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

        if (auction.getStatus() != AuctionStatus.CLOSED
                && auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Winner can only be determined for ACTIVE or CLOSED auctions. Current: "
                            + auction.getStatus());
        }

        Optional<Bid> highestBid = bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId);

        if (highestBid.isEmpty()) {
            // No bids — mark as CLOSED with no winner
            auction.setStatus(AuctionStatus.CLOSED);
            auctionRepository.save(auction);

            return AnalyticsDto.WinnerDeterminationResult.builder()
                    .auctionId(auctionId)
                    .auctionTitle(auction.getTitle())
                    .winnerUsername(null)
                    .winningBidAmount(null)
                    .auctionStatus(auction.getStatus().name())
                    .message("Auction closed with no bids. No winner determined.")
                    .build();
        }

        Bid winning = highestBid.get();

        // Update auction with winner info
        auction.setStatus(AuctionStatus.CLOSED);
        auction.setCurrentHighestBid(winning.getAmount().doubleValue());
        // currentHighestBidder is a User FK — set by username lookup if available
        // For now we store the winner name in the result; full integration needs User lookup
        auctionRepository.save(auction);

        return AnalyticsDto.WinnerDeterminationResult.builder()
                .auctionId(auctionId)
                .auctionTitle(auction.getTitle())
                .winnerUsername(winning.getBidderUsername())
                .winningBidAmount(winning.getAmount())
                .auctionStatus(auction.getStatus().name())
                .message("Winner determined: " + winning.getBidderUsername()
                        + " with bid of $" + winning.getAmount())
                .build();
    }

    /**
     * Runs winner determination for ALL auctions that are ACTIVE
     * but have passed their end time. Called by scheduler or manually.
     */
    @Transactional
    public List<AnalyticsDto.WinnerDeterminationResult> determineAllExpiredAuctionWinners() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> expired = auctionRepository.findByStatus(AuctionStatus.ACTIVE)
                .stream()
                .filter(a -> a.getEndAt() != null && a.getEndAt().isBefore(now))
                .collect(Collectors.toList());

        return expired.stream()
                .map(a -> determineWinner(a.getId()))
                .collect(Collectors.toList());
    }

    // ── 3. Transaction Records & Invoice ─────────────────────────────────────

    /** All transaction records (admin view) */
    public List<AnalyticsDto.TransactionRecord> getAllTransactionRecords() {
        return paymentRepository.findAll().stream()
                .map(this::toTransactionRecord)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Transaction record for one specific auction */
    public AnalyticsDto.TransactionRecord getTransactionByAuction(Long auctionId) {
        Payment payment = paymentRepository.findByOrder_Auction_Id(auctionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No transaction found for auction: " + auctionId));
        return toTransactionRecord(payment);
    }

    /** Invoice data for a specific order (used by frontend to generate PDF) */
    public AnalyticsDto.InvoiceData getInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payment found for order: " + orderId));

        Optional<PaymentTransaction> latestTxn =
                paymentTransactionRepository.findTopByPaymentIdOrderByCreatedAtDesc(payment.getId());

        Auction auction = order.getAuction();

        return AnalyticsDto.InvoiceData.builder()
                .invoiceNumber("INV-" + String.format("%06d", orderId))
                .orderId(orderId)
                .auctionId(auction != null ? auction.getId() : null)
                .auctionTitle(auction != null ? auction.getTitle() : "Unknown")
                .auctionDescription(auction != null ? auction.getDescription() : "")
                .buyerName(order.getBuyer() != null ? order.getBuyer().getName() : "Unknown")
                .buyerEmail(order.getBuyer() != null ? order.getBuyer().getEmail() : "Unknown")
                .sellerName(order.getSeller() != null ? order.getSeller().getName() : "Unknown")
                .amount(payment.getAmount() != null
                        ? BigDecimal.valueOf(payment.getAmount()) : BigDecimal.ZERO)
                .currency(payment.getCurrency() != null ? payment.getCurrency() : "USD")
                .paymentStatus(payment.getPaymentStatus() != null
                        ? payment.getPaymentStatus().name() : "UNKNOWN")
                .gatewayTrxId(latestTxn.map(PaymentTransaction::getGatewayTrxId).orElse("N/A"))
                .issuedAt(order.getCreatedAt())
                .paidAt(payment.getUpdatedAt())
                .build();
    }

    // ── 4. Bid Growth Trend Visualization ────────────────────────────────────

    public AnalyticsDto.BidGrowthTrend getBidGrowthTrend(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

        List<Bid> bids = bidRepository.findByAuctionIdOrderByPlacedAtAsc(auctionId);

        List<AnalyticsDto.BidDataPoint> points = new ArrayList<>();
        for (int i = 0; i < bids.size(); i++) {
            Bid b = bids.get(i);
            points.add(AnalyticsDto.BidDataPoint.builder()
                    .placedAt(b.getPlacedAt())
                    .amount(b.getAmount())
                    .bidderUsername(b.getBidderUsername())
                    .bidNumber(i + 1)
                    .build());
        }

        BigDecimal startingPrice = auction.getBidStartingPrice() != null
                ? BigDecimal.valueOf(auction.getBidStartingPrice()) : BigDecimal.ZERO;

        BigDecimal currentHighest = auction.getCurrentHighestBid() != null
                ? BigDecimal.valueOf(auction.getCurrentHighestBid()) : startingPrice;

        BigDecimal growthPercent = BigDecimal.ZERO;
        if (startingPrice.compareTo(BigDecimal.ZERO) > 0) {
            growthPercent = currentHighest.subtract(startingPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(startingPrice, 2, RoundingMode.HALF_UP);
        }

        return AnalyticsDto.BidGrowthTrend.builder()
                .auctionId(auctionId)
                .auctionTitle(auction.getTitle())
                .startingPrice(startingPrice)
                .currentHighestBid(currentHighest)
                .totalBids(bids.size())
                .bidProgression(points)
                .growthPercent(growthPercent)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AnalyticsDto.TransactionRecord toTransactionRecord(Payment payment) {
        if (payment == null || payment.getOrder() == null) return null;

        Order order = payment.getOrder();
        Auction auction = order.getAuction();

        Optional<PaymentTransaction> latestTxn =
                paymentTransactionRepository.findTopByPaymentIdOrderByCreatedAtDesc(payment.getId());

        return AnalyticsDto.TransactionRecord.builder()
                .transactionId(latestTxn.map(PaymentTransaction::getId).orElse(null))
                .orderId(order.getId())
                .auctionId(auction != null ? auction.getId() : null)
                .auctionTitle(auction != null ? auction.getTitle() : "Unknown")
                .winnerUsername(order.getBuyer() != null ? order.getBuyer().getName() : "Unknown")
                .sellerUsername(order.getSeller() != null ? order.getSeller().getName() : "Unknown")
                .paymentAmount(payment.getAmount() != null
                        ? BigDecimal.valueOf(payment.getAmount()) : BigDecimal.ZERO)
                .currency(payment.getCurrency())
                .paymentStatus(payment.getPaymentStatus() != null
                        ? payment.getPaymentStatus().name() : "UNKNOWN")
                .transactionStatus(latestTxn
                        .map(t -> t.getTransactionStatus() != null
                                ? t.getTransactionStatus().name() : "UNKNOWN")
                        .orElse("UNKNOWN"))
                .gatewayTrxId(latestTxn.map(PaymentTransaction::getGatewayTrxId).orElse("N/A"))
                .paidAt(payment.getUpdatedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
