package com.example.bidoo_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String path = request.getURI().getPath();
        log.info("WebSocket handshake request path: {}", path);
        
        // Extract userId from path: /ws/notifications/{userId}
        String[] parts = path.split("/");
        if (parts.length > 0) {
            String userIdStr = parts[parts.length - 1];
            try {
                Long userId = Long.parseLong(userIdStr);
                attributes.put("userId", userIdStr);
                log.info("WebSocket handshake successful for userId: {}", userId);
                return true;
            } catch (NumberFormatException e) {
                log.error("Invalid userId in WebSocket path: {}", userIdStr);
                return false;
            }
        }
        
        log.warn("Could not extract userId from WebSocket path: {}", path);
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake failed: {}", exception.getMessage());
        }
    }
}
