package com.ecommerce.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS CONFIGURATION - Cross-Origin Resource Sharing
 * 
 * PURPOSE (उद्देश्य):
 * - Frontend (React app on port 3000) से requests allow करना
 * - Browser security policy (CORS) handle करना
 * 
 * CORS CONCEPT:
 * - Browser security feature
 * - Different port/domain से requests block करता है (by default)
 * - API Gateway (port 8080) से React app (port 3000) को allow करना
 * 
 * WHY NEEDED:
 * - React app (localhost:3000) → API Gateway (localhost:8080)
 * - Different ports = Cross-origin request
 * - Without CORS → Browser blocks the request ❌
 * - With CORS → Browser allows the request ✅
 */
@Configuration // Spring configuration class
public class CorsConfig {
    
    /**
     * CORS WEB FILTER BEAN
     * 
     * Spring WebFlux (reactive) के लिए CORS filter
     * API Gateway uses reactive stack, so CorsWebFilter needed
     * 
     * CONFIGURATION:
     * - Allowed origins: React app (localhost:3000)
     * - Allowed methods: GET, POST, PUT, DELETE, etc.
     * - Allowed headers: All headers
     * - Exposed headers: Custom headers (X-Total-Count, Authorization)
     * - Credentials: Allow cookies/auth headers
     * 
     * @return CorsWebFilter - CORS filter for API Gateway
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allowed origins (frontend URLs)
        // React app runs on localhost:3000
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        
        // Cache preflight requests for 1 hour
        corsConfig.setMaxAge(3600L);
        
        // Allowed HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allowed request headers
        corsConfig.setAllowedHeaders(Arrays.asList("*")); // Allow all headers
        
        // Exposed headers (frontend can read these)
        // X-Total-Count: Pagination total count
        // Authorization: JWT token
        corsConfig.setExposedHeaders(Arrays.asList("X-Total-Count", "Content-Range", "Authorization"));
        
        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);
        
        // Register CORS configuration for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Apply to all endpoints
        
        return new CorsWebFilter(source);
    }
}
