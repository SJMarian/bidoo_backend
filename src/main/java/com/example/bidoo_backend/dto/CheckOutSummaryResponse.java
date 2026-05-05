package com.example.bidoo_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutSummaryResponse {
    private Double soldPrice;
    private Double vat;
    private Double platfromFee;
    private Double shippingCost;

}

