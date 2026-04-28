package com.example.bidoo_backend.web;

import com.example.bidoo_backend.service.UserActivityTrackingService;
import com.example.bidoo_backend.entity.UserActivityLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP request filter to track user activities
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityTrackingFilter extends OncePerRequestFilter {
    
    private final UserActivityTrackingService activityTrackingService;
    
    private static final String[] EXCLUDED_PATHS = {
        "/actuator",
        "/health",
        "/swagger-ui",
        "/api-docs",
        "/ws/"
    };
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Skip excluded paths
        if (shouldExcludePath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Only track authenticated users (not anonymous)
            if (authentication != null && 
                !(authentication instanceof AnonymousAuthenticationToken) &&
                authentication.isAuthenticated()) {
                
                String username = getUsername(authentication);
                Long userId = extractUserId(authentication);
                String ipAddress = getClientIP(request);
                String userAgent = request.getHeader("User-Agent");
                String method = request.getMethod();
                
                // Track specific activities
                trackActivity(method, path, username, userId, ipAddress, userAgent);
            }
        } catch (Exception e) {
            log.warn("Failed to track activity: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void trackActivity(String method, String path, String username, Long userId,
                              String ipAddress, String userAgent) {
        
        // Track dashboard access
        if (path.contains("/api/admin/analytics") && "GET".equalsIgnoreCase(method)) {
            activityTrackingService.logActivity(
                userId, username,
                UserActivityLog.ActivityType.DASHBOARD_ACCESSED,
                "Accessed analytics dashboard",
                ipAddress, userAgent
            );
        }
        
        // Track bid placement
        else if (path.contains("/api/bids") && "POST".equalsIgnoreCase(method)) {
            activityTrackingService.logActivity(
                userId, username,
                UserActivityLog.ActivityType.BID_PLACED,
                "Placed a bid",
                ipAddress, userAgent
            );
        }
        
        // Track auction creation
        else if (path.contains("/api/auctions") && "POST".equalsIgnoreCase(method)) {
            activityTrackingService.logActivity(
                userId, username,
                UserActivityLog.ActivityType.AUCTION_CREATED,
                "Created new auction",
                ipAddress, userAgent
            );
        }
        
        // Track payment
        else if (path.contains("/api/payments") && "POST".equalsIgnoreCase(method)) {
            activityTrackingService.logActivity(
                userId, username,
                UserActivityLog.ActivityType.PAYMENT_INITIATED,
                "Initiated payment",
                ipAddress, userAgent
            );
        }
        
        // Track report export
        else if (path.contains("/api/admin/export")) {
            String exportType = path.replaceAll(".*/api/admin/export/", "");
            activityTrackingService.logActivity(
                userId, username,
                UserActivityLog.ActivityType.REPORT_EXPORTED,
                "Exported report: " + exportType,
                ipAddress, userAgent
            );
        }
    }
    
    private String getUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
    
    private Long extractUserId(Authentication authentication) {
        // In a real application, extract from security context or JWT token
        // For now, return a default value
        return 1L;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    private boolean shouldExcludePath(String path) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.contains(excludedPath)) {
                return true;
            }
        }
        return false;
    }
}
