package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.ApiResponse;
import com.example.bidoo_backend.service.CurrencyConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyConversionService currencyConversionService;

    @GetMapping("/convert")
    public ResponseEntity<ApiResponse<Double>> convertCurrency(
            @RequestParam Double amount,
            @RequestParam String from,
            @RequestParam String to
    ) {
        Double convertedAmount = currencyConversionService.convert(amount, from, to);

        return ResponseEntity.ok(
                ApiResponse.success(convertedAmount, "Currency converted successfully", HttpStatus.OK.value())
        );
    }
}