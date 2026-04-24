package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.ApiResponse;
import com.example.bidoo_backend.dto.BidRequest;
import com.example.bidoo_backend.dto.BidResponse;
import com.example.bidoo_backend.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(
            @Valid @RequestBody BidRequest request,
            Authentication authentication) {

        try {
            BidResponse response = bidService.placeBid(request, authentication);

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Bid placed successfully", HttpStatus.OK.value())
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value())
            );
        }
    }
}