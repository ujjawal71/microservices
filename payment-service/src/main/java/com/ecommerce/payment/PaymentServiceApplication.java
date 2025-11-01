package com.ecommerce.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * PAYMENT SERVICE APPLICATION - Main Application Class
 * 
 * ========================================================================
 * FEIGN CLIENT ENABLED
 * ========================================================================
 * 
 * @EnableFeignClients:
 * - Enables Feign Client scanning
 * - Finds @FeignClient interfaces
 * - Creates proxies for inter-service communication
 * 
 * FEIGN CLIENTS USED:
 * - OrderClient: Call Order Service (update order status)
 */
@SpringBootApplication
@EnableDiscoveryClient // Register with Eureka Service Discovery
@EnableFeignClients // Enable Feign Clients for inter-service communication
public class PaymentServiceApplication {
    
    /**
     * MAIN METHOD
     * 
     * Starts Payment Service
     * 
     * @param args - Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
