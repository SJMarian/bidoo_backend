package com.example.bidoo_backend.service;

import com.example.bidoo_backend.config.SslCommerzConfig;
import com.example.bidoo_backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SslCommerzService {

    private final SslCommerzConfig sslCommerzConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public String initPayment(Double amount, String currency, String transactionId, User user, String itemTitle) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("store_id", sslCommerzConfig.getStoreId());
        map.add("store_passwd", sslCommerzConfig.getStorePasswd());
        map.add("total_amount", amount.toString());
        map.add("currency", currency != null && !currency.isEmpty() ? currency : "BDT");
        map.add("tran_id", transactionId);
        map.add("success_url", sslCommerzConfig.getSuccessUrl());
        map.add("fail_url", sslCommerzConfig.getFailUrl());
        map.add("cancel_url", sslCommerzConfig.getCancelUrl());
        
        // Customer info
        map.add("cus_name", user.getName() != null ? user.getName() : "Customer");
        map.add("cus_email", user.getEmail() != null ? user.getEmail() : "customer@example.com");
        map.add("cus_add1", user.getAddress() != null ? user.getAddress() : "Dhaka");
        map.add("cus_city", "Dhaka");
        map.add("cus_state", "Dhaka");
        map.add("cus_postcode", "1000");
        map.add("cus_country", "Bangladesh");
        map.add("cus_phone", user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "01700000000");

        // Shipping info
        map.add("shipping_method", "NO");
        map.add("num_of_item", "1");
        
        // Product info
        map.add("product_name", itemTitle != null ? itemTitle : "Auction Item");
        map.add("product_category", "Auction");
        map.add("product_profile", "general");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(sslCommerzConfig.getApiUrl(), request, Map.class);
            Map<String, Object> body = response.getBody();
            
            if (body != null) {
                if ("SUCCESS".equals(body.get("status"))) {
                    return (String) body.get("GatewayPageURL");
                }
                if (body.containsKey("failedreason")) {
                    System.out.println("SSLCommerz Init failed reason: " + body.get("failedreason"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}