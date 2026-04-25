package com.example.bidoo_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyConversionService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Double convert(Double amount, String fromCurrency, String toCurrency) {
        if (amount == null) {
            return 0.0;
        }

        if (fromCurrency == null || toCurrency == null || fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        String url = "https://api.exchangerate-api.com/v4/latest/" + fromCurrency;

        Map response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("rates")) {
            throw new RuntimeException("Currency conversion failed");
        }

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");

        Object rateObject = rates.get(toCurrency.toUpperCase());

        if (rateObject == null) {
            throw new RuntimeException("Currency not supported: " + toCurrency);
        }

        Double rate = Double.valueOf(rateObject.toString());

        return amount * rate;
    }
}