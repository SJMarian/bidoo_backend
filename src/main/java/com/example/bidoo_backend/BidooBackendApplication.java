package com.example.bidoo_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BidooBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BidooBackendApplication.class, args);
    }
}
