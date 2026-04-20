package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.ApiResponse;
import com.example.bidoo_backend.dto.CheckOutSummaryResponse;
import com.example.bidoo_backend.dto.OrderRequestDTO;
import com.example.bidoo_backend.entity.*;
import com.example.bidoo_backend.enums.OrderStatus;
import com.example.bidoo_backend.enums.PaymentStatus;
import com.example.bidoo_backend.enums.TransactionStatus;
import com.example.bidoo_backend.repository.*;
import com.example.bidoo_backend.service.SslCommerzService;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
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
    private final AuctionItemRepository auctionItemRepository;
    private final SslCommerzService sslCommerzService;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> placeOrder(
            @Valid @RequestBody OrderRequestDTO request, Principal principal) {

        User buyer = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        AuctionItem auctionItem = auctionItemRepository.findById(request.getAuctionItemId())
                .orElseThrow(() -> new IllegalArgumentException("Auction item not found"));

        // TEMP comment code. remove after testing ----

        // if (!buyer.equals(auctionItem.getCurrentHighestBidder())) {
        // return ResponseEntity.badRequest().body(ApiResponse.error("Only the highest
        // bidder can place an order", HttpStatus.BAD_REQUEST.value()));
        // }

        // Temp comment code end ---------------

        Order order;
        Payment payment;
        
        // Check if item is already ordered
        Optional<Order> existingOrderOpt = orderRepository.findByAuctionItemId(auctionItem.getId());
        if (existingOrderOpt.isPresent()) {
            order = existingOrderOpt.get();
            if (order.getStatus() == OrderStatus.PAID) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Order is already paid for this item", HttpStatus.BAD_REQUEST.value()));
            }
            // Reuse existing order
            order.setStatus(OrderStatus.PENDING);
            order.setFinalPrice(auctionItem.getCurrentHighestBid() != null ? auctionItem.getCurrentHighestBid() : 0.0);
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
                        .currency(auctionItem.getCurrency() != null ? auctionItem.getCurrency() : "BDT")
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
                    .auctionItem(auctionItem)
                    .buyer(buyer)
                    .seller(auctionItem.getSeller())
                    .finalPrice(auctionItem.getCurrentHighestBid() != null ? auctionItem.getCurrentHighestBid() : 0.0)
                    .status(OrderStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
    
            order = orderRepository.save(order);
    
            // Create Payment
            payment = Payment.builder()
                    .order(order)
                    .user(buyer)
                    .amount(order.getFinalPrice())
                    .currency(auctionItem.getCurrency() != null ? auctionItem.getCurrency() : "BDT")
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
                auctionItem.getTitle());

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
    public void paymentSuccess(@RequestParam Map<String, String> formData,  HttpServletResponse response)throws IOException, java.io.IOException  {
        String tranId = formData.get("tran_id");
        if (tranId == null) {
            response.sendRedirect(frontendBaseUrl + "/payment-fail?error=missing_tran_id");
            return;
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

            response.sendRedirect(frontendBaseUrl + "/payment-success?tran_id=" + tranId);
            return;
        }

        response.sendRedirect(frontendBaseUrl + "/payment-fail?error=something_went_wrong");
        return;
    }

    @PostMapping(value = "/payment/fail", consumes = "application/x-www-form-urlencoded")
    public void paymentFail(@RequestParam Map<String, String> formData, HttpServletResponse response)throws IOException, java.io.IOException  {
        String tranId = formData.get("tran_id");
        if (tranId == null) {
            response.sendRedirect(frontendBaseUrl + "/payment-fail?error=missing_tran_id");
            return;
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

            response.sendRedirect(frontendBaseUrl + "/payment-fail?tran_id=" + tranId);
            return;
        }
        response.sendRedirect(frontendBaseUrl + "/payment-fail?error=not_found");
        return;
    }

    @PostMapping(value = "/payment/cancel", consumes = "application/x-www-form-urlencoded")
    public void paymentCancel(@RequestParam Map<String, String> formData, HttpServletResponse response)throws IOException, java.io.IOException  {
        String tranId = formData.get("tran_id");
        if (tranId == null) {
            response.sendRedirect(frontendBaseUrl + "/payment-cancel?error=missing_tran_id");
            return;
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

            response.sendRedirect(frontendBaseUrl + "/payment-cancel?tran_id="+tranId);
            return;
        }

        response.sendRedirect(frontendBaseUrl + "/payment-cancel?error=not_found");
        return;
    }


    @PostMapping(value = "/checkout/summary")
    public ResponseEntity<ApiResponse<CheckOutSummaryResponse>> checkoutSummary(
            @Valid @RequestBody OrderRequestDTO request, Principal principal) {

        AuctionItem auctionItem = auctionItemRepository.findById(request.getAuctionItemId())
                .orElseThrow(() -> new IllegalArgumentException("Auction item not found"));
                
                double currentBid = auctionItem.getCurrentHighestBid();

                double vat = currentBid * 0.15;

                // Apply minimums
                double platformFee = currentBid * 0.02;
                if (platformFee == 0) {
                    platformFee = 200;
                }

                double shippingCost = currentBid * 0.05;
                if (shippingCost == 0) {
                    shippingCost = 500;
}

                final CheckOutSummaryResponse  response = CheckOutSummaryResponse.builder()
                .soldPrice(currentBid)
                .vat(vat)
                .platfromFee(platformFee)
                .shippingCost(shippingCost)
                .build();

                return ResponseEntity
                    .ok(ApiResponse.success(response, "Success", HttpStatus.OK.value()));

            }

}
