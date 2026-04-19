package com.example.bidoo_backend.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrencyResponse {

    private Double bdt;
    private Double usd;
    private Double eur;
}