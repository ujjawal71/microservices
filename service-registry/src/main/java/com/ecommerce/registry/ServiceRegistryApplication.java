package com.ecommerce.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * SERVICE REGISTRY APPLICATION - Eureka Server
 * 
 * ========================================================================
 * SERVICE DISCOVERY CONCEPT
 * ========================================================================
 * 
 * EUREKA SERVER:
 * - Service registry for microservices
 * - Maintains list of all available services
 * - Services register themselves on startup
 * - Services can discover other services by name
 * 
 * HOW IT WORKS:
 * 1. Services register with Eureka Server on startup
 * 2. Services send heartbeats to Eureka (health check)
 * 3. Eureka maintains service registry
 * 4. When service wants to call another service:
 *    - Query Eureka: "Where is product-service?"
 *    - Eureka returns: "http://product-service:8082"
 *    - Service makes call using discovered URL
 * 
 * BENEFITS:
 * - No hardcoded URLs
 * - Load balancing (multiple instances)
 * - Automatic health monitoring
 * - Service failover
 * 
 * ========================================================================
 * MICROSERVICES PATTERN
 * ========================================================================
 * 
 * SERVICE REGISTRY PATTERN:
 * - Central registry of all services
 * - Services register on startup
 * - Services discover others at runtime
 * - Dynamic service location
 * 
 * ALTERNATIVES:
 * - Consul (HashiCorp)
 * - Zookeeper (Apache)
 * - Kubernetes Service Discovery
 */
@SpringBootApplication // Main Spring Boot application
@EnableEurekaServer // Enable Eureka Server (Service Registry)
public class ServiceRegistryApplication {
    
    /**
     * MAIN METHOD
     * 
     * Starts Eureka Service Registry Server
     * 
     * PORT: 8761 (default Eureka port)
     * URL: http://localhost:8761
     * 
     * DASHBOARD:
     * - Eureka dashboard shows all registered services
     * - Service health status
     * - Service instances
     * 
     * @param args - Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
