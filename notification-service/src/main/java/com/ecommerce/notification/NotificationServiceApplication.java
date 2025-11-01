package com.ecommerce.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * NOTIFICATION SERVICE APPLICATION - Main Application Class
 * 
 * ========================================================================
 * EVENT-DRIVEN SERVICE
 * ========================================================================
 * 
 * PURPOSE:
 * - Listens to Kafka events
 * - Sends notifications (Email, SMS)
 * - Asynchronous event processing
 * 
 * KAFKA CONSUMER:
 * - @KafkaListener annotations in NotificationService
 * - Subscribes to topics: "order-created", "payment-completed"
 */
@SpringBootApplication
@EnableDiscoveryClient // Register with Eureka Service Discovery
public class NotificationServiceApplication {
    
    /**
     * MAIN METHOD
     * 
     * Starts Notification Service
     * 
     * @param args - Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
