package com.example.bidoo_backend.auction.controller;

import com.example.bidoo_backend.auction.dto.BidRequest;
import com.example.bidoo_backend.auction.dto.BidResponse;
import com.example.bidoo_backend.auction.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<BidResponse> placeBid(
            @RequestBody BidRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                bidService.placeBid(request, principal)
        );
    }
}