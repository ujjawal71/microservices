package com.ecommerce.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * USER SERVICE APPLICATION - Main Application Class
 * 
 * ========================================================================
 * SPRING BOOT APPLICATION
 * ========================================================================
 * 
 * @SpringBootApplication:
 * - Main entry point for Spring Boot application
 * - Combines: @Configuration, @EnableAutoConfiguration, @ComponentScan
 * - Auto-configuration: Spring Boot configures everything automatically
 * 
 * SPRING BOOT AUTO-CONFIGURATION:
 * - Database connection (JPA, Hibernate)
 * - Web server (Tomcat)
 * - Security configuration
 * - JSON serialization
 * 
 * ========================================================================
 * SERVICE DISCOVERY (EUREKA CLIENT)
 * ========================================================================
 * 
 * @EnableDiscoveryClient:
 * - Registers this service with Eureka Server
 * - Service name: "user-service" (from application.yml)
 * - Port: 8081 (from application.yml)
 * 
 * SERVICE REGISTRATION:
 * - On startup, service registers with Eureka Server
 * - Eureka Server maintains service registry
 * - Other services can discover this service by name
 * 
 * SERVICE DISCOVERY BENEFITS:
 * - No hardcoded URLs (services found by name)
 * - Load balancing (multiple instances)
 * - Health monitoring
 * 
 * ========================================================================
 * MICROSERVICES ARCHITECTURE
 * ========================================================================
 * 
 * INDEPENDENT SERVICE:
 * - Runs on separate port (8081)
 * - Own database (users table)
 * - Own business logic
 * - Can be scaled independently
 * 
 * COMMUNICATION:
 * - Other services call this service through API Gateway
 * - Or directly using Feign Client (service discovery)
 */
@SpringBootApplication // Main Spring Boot application
@EnableDiscoveryClient // Register with Eureka Service Discovery
public class UserServiceApplication {
    
    /**
     * MAIN METHOD
     * 
     * Application entry point
     * 
     * SPRING BOOT STARTUP:
     * 1. SpringApplication.run() starts the application
     * 2. Auto-configuration runs
     * 3. Components scanned and registered
     * 4. Embedded Tomcat server starts
     * 5. Service registers with Eureka
     * 6. Application ready to accept requests
     * 
     * @param args - Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
