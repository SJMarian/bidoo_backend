package com.example.bidoo_backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SslCommerzConfig {
    @Value("${sslcommerz.store-id:testbox}")
    private String storeId;

    @Value("${sslcommerz.store-passwd:testpassword}")
    private String storePasswd;

    @Value("${sslcommerz.is-sandbox:true}")
    private boolean isSandbox;

    @Value("${sslcommerz.success-url:http://localhost:8080/api/v1/orders/payment/success}")
    private String successUrl;

    @Value("${sslcommerz.fail-url:http://localhost:8080/api/v1/orders/payment/fail}")
    private String failUrl;

    @Value("${sslcommerz.cancel-url:http://localhost:8080/api/v1/orders/payment/cancel}")
    private String cancelUrl;

    public String getApiUrl() {
        return isSandbox ? "https://sandbox.sslcommerz.com/gwprocess/v4/api.php"
                         : "https://securepay.sslcommerz.com/gwprocess/v4/api.php";
    }
}