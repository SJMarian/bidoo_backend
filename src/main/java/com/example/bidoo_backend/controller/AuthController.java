package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.ApiResponse;
import com.example.bidoo_backend.dto.AuthRequest;
import com.example.bidoo_backend.dto.AuthResponse;
import com.example.bidoo_backend.dto.RegisterRequest;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    // ✅ REGISTER (FINAL SIMPLE VERSION)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Email already exists", HttpStatus.BAD_REQUEST.value()));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());
        user.setAddress("Dhaka"); // optional
        user.setRole("USER"); // ✅ STRING FIX

        userRepository.save(user);

        AuthResponse responseData = AuthResponse.builder()
                .token("dummy-token") // ✅ no JWT needed
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(responseData, "User registered successfully", HttpStatus.OK.value()));
    }

    // ✅ LOGIN (FINAL SIMPLE VERSION)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Invalid password", HttpStatus.UNAUTHORIZED.value()));
        }

        AuthResponse responseData = AuthResponse.builder()
                .token("dummy-token") // ✅ no JWT
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(responseData, "Login successful", HttpStatus.OK.value()));
    }
}