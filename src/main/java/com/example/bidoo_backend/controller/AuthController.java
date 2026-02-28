package com.example.bidoo_backend.controller;

import com.example.bidoo_backend.dto.ApiResponse;
import com.example.bidoo_backend.dto.AuthRequest;
import com.example.bidoo_backend.dto.AuthResponse;
import com.example.bidoo_backend.dto.RegisterRequest;
import com.example.bidoo_backend.entity.Role;
import com.example.bidoo_backend.entity.User;
import com.example.bidoo_backend.repository.UserRepository;
import com.example.bidoo_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Email is already taken", HttpStatus.BAD_REQUEST.value()));
        }

        Role userRole = Role.USER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("admin")) {
            userRole = Role.ADMIN;
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .build();

        userRepository.save(user);

        String jwtToken = jwtUtil.generateToken(user);
        AuthResponse responseData = AuthResponse.builder().token(jwtToken).build();
        return ResponseEntity.ok(
                ApiResponse.success(responseData, "User registered successfully", HttpStatus.OK.value()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Invalid email or password", HttpStatus.UNAUTHORIZED.value()));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String jwtToken = jwtUtil.generateToken(user);
        AuthResponse responseData = AuthResponse.builder().token(jwtToken).build();
        return ResponseEntity.ok(
                ApiResponse.success(responseData, "Login successful", HttpStatus.OK.value()));
    }
}
