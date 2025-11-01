package com.ecommerce.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * SECURITY CONFIGURATION - Spring Security Setup
 * 
 * ========================================================================
 * SECURITY CONCEPTS
 * ========================================================================
 * 
 * 1. STATELESS AUTHENTICATION:
 *    - JWT-based authentication (no server-side sessions)
 *    - Token stored on client (localStorage)
 *    - Stateless = Scalable (no session storage needed)
 * 
 * 2. CORS (Cross-Origin Resource Sharing):
 *    - Allow requests from frontend (localhost:3000)
 *    - Browser security policy handling
 * 
 * 3. PASSWORD ENCODING:
 *    - BCrypt algorithm (one-way hashing)
 *    - Salt automatically added
 *    - Secure password storage
 * 
 * 4. CSRF PROTECTION:
 *    - Disabled for stateless API (JWT tokens provide protection)
 *    - Enabled for stateful applications
 * 
 * ========================================================================
 * SPRING SECURITY FILTER CHAIN
 * ========================================================================
 * 
 * Request Flow:
 * Client → Security Filters → Controller
 * 
 * Filters:
 * - CORS Filter (handle cross-origin)
 * - Authentication Filter (JWT validation)
 * - Authorization Filter (role-based access)
 */
@Configuration // Spring configuration class
@EnableWebSecurity // Enable Spring Security
public class SecurityConfig {
    
    /**
     * SECURITY FILTER CHAIN
     * 
     * Configures Spring Security behavior
     * 
     * CONFIGURATION:
     * - CORS: Allow cross-origin requests
     * - CSRF: Disabled (stateless API with JWT)
     * - Session: Stateless (no server-side sessions)
     * - Authorization: Public endpoints + authenticated endpoints
     * 
     * @param http - HttpSecurity builder
     * @return SecurityFilterChain - Configured security filter chain
     * @throws Exception - If configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS Configuration
            // Allow requests from frontend (React app)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF Protection
            // Disabled because:
            // 1. Stateless API (no server-side sessions)
            // 2. JWT tokens provide protection
            // 3. Frontend and API on different ports (CORS handles it)
            .csrf(csrf -> csrf.disable())
            
            // Session Management
            // STATELESS: No server-side sessions
            // JWT tokens used for authentication (stored on client)
            // Benefits: Scalable, no session storage needed
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization Rules
            // Public endpoints: /api/auth/**, /actuator/**, /error
            // Protected endpoints: All other endpoints require authentication
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/**", "/error").permitAll() // Public endpoints
                .anyRequest().authenticated() // All other endpoints require authentication
            )
            
            // HTTP Headers Security
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()) // Allow iframes (if needed)
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")) // Content Security Policy
            );
        return http.build();
    }
    
    /**
     * CORS CONFIGURATION SOURCE
     * 
     * Configures Cross-Origin Resource Sharing
     * 
     * CORS CONCEPT:
     * - Browser security feature
     * - Blocks requests from different origin (port/domain)
     * - API Gateway (port 8080) → Frontend (port 3000) = Cross-origin
     * 
     * CONFIGURATION:
     * - Allowed origins: Frontend URLs
     * - Allowed methods: HTTP methods
     * - Allowed headers: Request headers
     * - Credentials: Allow cookies/auth headers
     * 
     * @return CorsConfigurationSource - CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins (frontend URLs)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        
        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allowed request headers
        configuration.setAllowedHeaders(Arrays.asList("*")); // Allow all headers
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        // Register CORS configuration for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * PASSWORD ENCODER BEAN
     * 
     * BCrypt Password Encoder
     * 
     * BCrypt ALGORITHM:
     * - One-way hashing algorithm
     * - Same password = different hash (salt added automatically)
     * - Cannot reverse/decode (secure)
     * - Slow by design (prevents brute force attacks)
     * 
     * HOW IT WORKS:
     * 1. User registers with password "password123"
     * 2. BCrypt hashes it: "$2a$10$abc123..." (with salt)
     * 3. Hash stored in database (never plain password)
     * 4. User logs in with "password123"
     * 5. BCrypt hashes input and compares with stored hash
     * 6. If match → login successful
     * 
     * SECURITY:
     * - Salt prevents rainbow table attacks
     * - Slow hashing prevents brute force
     * - Industry standard algorithm
     * 
     * @return PasswordEncoder - BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt algorithm (secure hashing)
    }
}
