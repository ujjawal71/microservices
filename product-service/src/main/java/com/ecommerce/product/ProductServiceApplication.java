package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * PRODUCT SERVICE APPLICATION - Main Application Class
 * 
 * ========================================================================
 * CIRCUIT BREAKER SUPPORT
 * ========================================================================
 * 
 * @EnableAspectJAutoProxy:
 * - Enables AOP (Aspect-Oriented Programming)
 * - Required for Resilience4j Circuit Breaker
 * - Circuit Breaker uses AOP for method interception
 */
@SpringBootApplication
@EnableDiscoveryClient // Register with Eureka Service Discovery
@EnableFeignClients // Enable Feign Clients (if needed)
@EnableAspectJAutoProxy // Enable AOP for Circuit Breaker
public class ProductServiceApplication {
    
    /**
     * MAIN METHOD
     * 
     * Starts Product Service
     * 
     * @param args - Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
