package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.ApiResponse;
import com.example.bidoo_backend.dto.OrderRequestDTO;
import com.example.bidoo_backend.entity.*;
import com.example.bidoo_backend.enums.OrderStatus;
import com.example.bidoo_backend.enums.PaymentStatus;
import com.example.bidoo_backend.enums.TransactionStatus;
import com.example.bidoo_backend.repository.*;
import com.example.bidoo_backend.service.SslCommerzService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final SslCommerzService sslCommerzService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> placeOrder(
            @Valid @RequestBody OrderRequestDTO request, Principal principal) {

        User buyer = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Auction auction = auctionRepository.findById(request.getAuctionItemId())
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        // TEMP comment code. remove after testing ----

        // if (!buyer.equals(auction.getCurrentHighestBidder())) {
        // return ResponseEntity.badRequest().body(ApiResponse.error("Only the highest
        // bidder can place an order", HttpStatus.BAD_REQUEST.value()));
        // }

        // Temp comment code end ---------------

        Order order;
        Payment payment;
        
        // Check if item is already ordered
        Optional<Order> existingOrderOpt = orderRepository.findByAuctionId(auction.getId());
        if (existingOrderOpt.isPresent()) {
            order = existingOrderOpt.get();
            if (order.getStatus() == OrderStatus.PAID) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Order is already paid for this item", HttpStatus.BAD_REQUEST.value()));
            }
            // Reuse existing order
            order.setStatus(OrderStatus.PENDING);
            order.setFinalPrice(auction.getCurrentHighestBid() != null ? auction.getCurrentHighestBid() : 0.0);
            order = orderRepository.save(order);
            
            Optional<Payment> existingPaymentOpt = paymentRepository.findByOrderId(order.getId());
            if (existingPaymentOpt.isPresent()) {
                payment = existingPaymentOpt.get();
                payment.setAmount(order.getFinalPrice());
                payment.setPaymentStatus(PaymentStatus.PENDING);
                payment.setUpdatedAt(LocalDateTime.now());
                payment = paymentRepository.save(payment);
            } else {
                payment = Payment.builder()
                        .order(order)
                        .user(buyer)
                        .amount(order.getFinalPrice())
                        .currency(auction.getCurrency() != null ? auction.getCurrency() : "BDT")
                        .paymentMethod("SSLCOMMERZ")
                        .gateway("SSLCOMMERZ")
                        .paymentStatus(PaymentStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                payment = paymentRepository.save(payment);
            }
        } else {
            // Create Order
            order = Order.builder()
                    .auction(auction)
                    .buyer(buyer)
                    .seller(auction.getSeller())
                    .finalPrice(auction.getCurrentHighestBid() != null ? auction.getCurrentHighestBid() : 0.0)
                    .status(OrderStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
    
            order = orderRepository.save(order);
    
            // Create Payment
            payment = Payment.builder()
                    .order(order)
                    .user(buyer)
                    .amount(order.getFinalPrice())
                    .currency(auction.getCurrency() != null ? auction.getCurrency() : "BDT")
                    .paymentMethod("SSLCOMMERZ")
                    .gateway("SSLCOMMERZ")
                    .paymentStatus(PaymentStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
    
            payment = paymentRepository.save(payment);
        }

        // Generate Transaction ID
        String tranId = UUID.randomUUID().toString();

        // Create Transaction
        PaymentTransaction transaction = PaymentTransaction.builder()
                .payment(payment)
                .gatewayTrxId(tranId)
                .createdAt(LocalDateTime.now())
                .build();

        paymentTransactionRepository.save(transaction);

        // Initialize SSLCommerz Payment Session
        String gatewayUrl = sslCommerzService.initPayment(
                payment.getAmount(),
                payment.getCurrency(),
                tranId,
                buyer,
                auction.getTitle());

        if (gatewayUrl != null) {
            return ResponseEntity
                    .ok(ApiResponse.success(gatewayUrl, "Redirect to this URL to pay", HttpStatus.OK.value()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to initialize payment gateway",
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping(value = "/payment/success", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<String> paymentSuccess(@RequestParam Map<String, String> formData) {
        String tranId = formData.get("tran_id");
        if (tranId == null) {
            return ResponseEntity.badRequest().body("Transaction ID missing");
        }

        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findByGatewayTrxId(tranId);
        if (transactionOpt.isPresent()) {
            PaymentTransaction transaction = transactionOpt.get();
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transaction.setGatewayResponse(formData.toString());
            paymentTransactionRepository.save(transaction);

            Payment payment = transaction.getPayment();
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            return ResponseEntity.ok("Payment Successful! Your order has been placed successfully.");
        }

        return ResponseEntity.badRequest().body("Transaction not found");
    }

    @PostMapping(value = "/payment/fail", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<String> paymentFail(@RequestParam Map<String, String> formData) {
        String tranId = formData.get("tran_id");
        if (tranId == null) {
            return ResponseEntity.badRequest().body("Transaction ID missing");
        }

        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findByGatewayTrxId(tranId);
        if (transactionOpt.isPresent()) {
            PaymentTransaction transaction = transactionOpt.get();
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setGatewayResponse(formData.toString());
            paymentTransactionRepository.save(transaction);

            Payment payment = transaction.getPayment();
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            return ResponseEntity.ok("Payment Failed! Your order has been cancelled.");
        }

        return ResponseEntity.badRequest().body("Transaction not found");
    }

    @PostMapping(value = "/payment/cancel", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<String> paymentCancel(@RequestParam Map<String, String> formData) {
        String tranId = formData.get("tran_id");
        if (tranId == null) {
            return ResponseEntity.badRequest().body("Transaction ID missing");
        }

        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findByGatewayTrxId(tranId);
        if (transactionOpt.isPresent()) {
            PaymentTransaction transaction = transactionOpt.get();
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setGatewayResponse(formData.toString());
            paymentTransactionRepository.save(transaction);

            Payment payment = transaction.getPayment();
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            return ResponseEntity.ok("Payment Cancelled.");
        }

        return ResponseEntity.badRequest().body("Transaction not found");
    }
}