package com.example.bidoo_backend.auction.service;

import com.example.bidoo_backend.auction.dto.CurrencyResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CurrencyService {

    public CurrencyResponse convert(Double bdtAmount) {

        String url = "https://open.er-api.com/v6/latest/BDT";

        RestTemplate restTemplate = new RestTemplate();
        Map response = restTemplate.getForObject(url, Map.class);

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");

        double usdRate = Double.parseDouble(rates.get("USD").toString());
        double eurRate = Double.parseDouble(rates.get("EUR").toString());

        return new CurrencyResponse(
                bdtAmount,
                bdtAmount * usdRate,
                bdtAmount * eurRate
        );
    }
}