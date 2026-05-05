package com.example.bidoo_backend.websocket;

import com.example.bidoo_backend.dto.NotificationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper;
    
    // Store active WebSocket sessions per userId
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        if (userId != null) {
            try {
                Long userIdLong = Long.parseLong(userId);
                userSessions.put(userIdLong, session);
                log.info("WebSocket connection established for user: {}", userId);
            } catch (NumberFormatException e) {
                log.error("Invalid userId format: {}", userId);
                session.close(CloseStatus.NOT_ACCEPTABLE);
            }
        }
    }
    
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received message: {}", payload);
        
        // Handle ping/pong for keep-alive
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = extractUserId(session);
        if (userId != null) {
            try {
                Long userIdLong = Long.parseLong(userId);
                userSessions.remove(userIdLong);
                log.info("WebSocket connection closed for user: {}", userId);
            } catch (NumberFormatException e) {
                log.error("Invalid userId format: {}", userId);
            }
        }
    }
    
    /**
     * Send a notification to a specific user via WebSocket
     */
    public void sendNotificationToUser(Long userId, NotificationDTO notification) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(json));
                log.debug("Notification sent to user {} via WebSocket", userId);
            } catch (IOException e) {
                log.error("Error sending notification to user {}: {}", userId, e.getMessage());
                userSessions.remove(userId);
            }
        }
    }
    
    /**
     * Broadcast a notification to all connected users
     */
    public void broadcastNotification(NotificationDTO notification) {
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    String json = objectMapper.writeValueAsString(notification);
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.error("Error broadcasting notification: {}", e.getMessage());
                    userSessions.remove(userId);
                }
            }
        });
    }
    
    private String extractUserId(WebSocketSession session) {
        return (String) session.getAttributes().get("userId");
    }
}
