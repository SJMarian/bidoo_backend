package com.example.bidoo_backend.auction.controller;

import com.example.bidoo_backend.auction.dto.CurrencyResponse;
import com.example.bidoo_backend.auction.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/convert")
    public CurrencyResponse convert(@RequestParam Double amount) {
        return currencyService.convert(amount);
    }
}