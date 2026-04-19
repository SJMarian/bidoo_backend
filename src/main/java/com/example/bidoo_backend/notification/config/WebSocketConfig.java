package com.example.bidoo_backend.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket (STOMP) configuration for real-time notification delivery.
 *
 * Client connects to: ws://localhost:8080/ws
 * Subscribe to personal queue: /user/queue/notifications
 * Server pushes to user via: messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", dto)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory broker for user-specific queues and topics
        registry.enableSimpleBroker("/queue", "/topic");
        // Prefix for messages bound for @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix used by convertAndSendToUser — maps to /user/{userId}/queue/...
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS fallback for browsers that don't support WebSocket
    }
}
