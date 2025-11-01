package com.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API GATEWAY APPLICATION - Main Application Class
 * 
 * ========================================================================
 * API GATEWAY PATTERN
 * ========================================================================
 * 
 * PURPOSE:
 * - Single entry point for all client requests
 * - Routes requests to appropriate microservices
 * - Load balancing
 * - Cross-cutting concerns (CORS, authentication, rate limiting)
 * 
 * BENEFITS:
 * - Clients don't need to know individual service URLs
 * - Centralized authentication/authorization
 * - Request routing and load balancing
 * - CORS handling
 * - Circuit breaker integration
 * 
 * ========================================================================
 * REQUEST FLOW
 * ========================================================================
 * 
 * Client Request:
 * Frontend → API Gateway (port 8080) → Microservice (discovered via Eureka)
 * 
 * EXAMPLE:
 * GET http://localhost:8080/api/products
 * ↓
 * API Gateway receives request
 * ↓
 * Routes to Product Service (discovered via Eureka)
 * ↓
 * Product Service processes request
 * ↓
 * Response returned through API Gateway
 * 
 * ========================================================================
 * SERVICE DISCOVERY
 * ========================================================================
 * 
 * @EnableDiscoveryClient:
 * - API Gateway registers with Eureka
 * - Can discover other services
 * - Routes requests using service names (not hardcoded URLs)
 * 
 * ROUTING:
 * - /api/products/** → product-service
 * - /api/orders/** → order-service
 * - /api/users/** → user-service
 * - Service names resolved via Eureka
 */
@SpringBootApplication // Main Spring Boot application
@EnableDiscoveryClient // Register with Eureka and enable service discovery
public class ApiGatewayApplication {
    
    /**
     * MAIN METHOD
     * 
     * Starts API Gateway server
     * 
     * PORT: 8080 (default configured in application.yml)
     * 
     * STARTUP:
     * 1. Spring Boot application starts
     * 2. Gateway routes configured
     * 3. Service discovery enabled
     * 4. Ready to route requests
     * 
     * @param args - Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
