package com.ecommerce.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * INVENTORY SERVICE APPLICATION - Main Application Class
 * 
 * ========================================================================
 * SERVICE DISCOVERY
 * ========================================================================
 * 
 * @EnableDiscoveryClient:
 * - Registers with Eureka Server
 * - Service name: "inventory-service"
 * - Port: 8085
 * 
 * SERVICE COMMUNICATION:
 * - Other services can discover this service
 * - Called via API Gateway or Feign Client
 */
@SpringBootApplication
@EnableDiscoveryClient // Register with Eureka Service Discovery
public class InventoryServiceApplication {
    
    /**
     * MAIN METHOD
     * 
     * Starts Inventory Service
     * 
     * @param args - Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
