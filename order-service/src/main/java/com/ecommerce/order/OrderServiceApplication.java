package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * ORDER SERVICE APPLICATION - Main Application Class
 * 
 * ========================================================================
 * MICROSERVICES CONCEPTS ENABLED
 * ========================================================================
 * 
 * 1. SERVICE DISCOVERY (@EnableDiscoveryClient):
 *    - Registers with Eureka Server
 *    - Other services can discover this service
 * 
 * 2. FEIGN CLIENT (@EnableFeignClients):
 *    - Enables Feign Client for inter-service communication
 *    - Declarative HTTP clients
 *    - Service Discovery integration
 * 
 * 3. ASPECT ORIENTED PROGRAMMING (@EnableAspectJAutoProxy):
 *    - Enables AOP for Circuit Breaker
 *    - Resilience4j uses AOP for method interception
 *    - Automatic aspect weaving
 * 
 * ========================================================================
 * INTER-SERVICE COMMUNICATION
 * ========================================================================
 * 
 * THIS SERVICE CALLS:
 * - Product Service (Feign Client) - Fetch product details
 * 
 * THIS SERVICE PUBLISHES EVENTS:
 * - "order-created" → Inventory Service, Notification Service consume
 * 
 * SAGA PATTERN:
 * - Order creation spans multiple services
 * - Distributed transaction management
 * - Eventual consistency
 */
@SpringBootApplication // Main Spring Boot application
@EnableDiscoveryClient // Register with Eureka Service Discovery
@EnableFeignClients // Enable Feign Clients for inter-service communication
@EnableAspectJAutoProxy // Enable AOP for Circuit Breaker (Resilience4j)
public class OrderServiceApplication {
    
    /**
     * MAIN METHOD
     * 
     * Application entry point
     * 
     * STARTUP SEQUENCE:
     * 1. Spring Boot application starts
     * 2. Feign Clients initialized
     * 3. Circuit Breaker aspects configured
     * 4. Service registers with Eureka
     * 5. Ready to accept requests
     * 
     * @param args - Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
