package com.example.bidoo_backend.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables Spring's @Async processing so the NotificationEventListener
 * can handle events without blocking the originating transaction.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot auto-configures a ThreadPoolTaskExecutor
    // based on spring.task.execution.* properties in application.properties
}
