package com.example.bidoo_backend.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

/**
 * Intercepts STOMP CONNECT frames to extract the userId header
 * and set it as the WebSocket session's Principal.
 *
 * This allows convertAndSendToUser(userId, ...) to route messages
 * to the correct user session.
 *
 * Frontend must send the userId as a STOMP connect header:
 *   stompClient.connect({ userId: '42' }, ...)
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureClientInboundChannel(
            org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String userId = accessor.getFirstNativeHeader("userId");
                    if (userId != null && !userId.isBlank()) {
                        // Set the Principal so convertAndSendToUser knows who this session belongs to
                        accessor.setUser(new Principal() {
                            @Override
                            public String getName() {
                                return userId;
                            }
                        });
                    }
                }
                return message;
            }
        });
    }
}
