package com.example.bidoo_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Bidoo backend is running successfully";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}