package com.ecommerce.payment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAZORPAY CONFIGURATION - Payment Gateway Client Setup
 * 
 * ========================================================================
 * EXTERNAL SERVICE INTEGRATION
 * ========================================================================
 * 
 * RAZORPAY:
 * - Third-party payment gateway (India)
 * - Handles payment processing securely
 * - Provides API for payment operations
 * 
 * CONFIGURATION:
 * - API Key ID: Public key (identifies merchant)
 * - API Secret: Private key (authenticates requests)
 * 
 * ========================================================================
 * DEPENDENCY INJECTION
 * ========================================================================
 * 
 * @Value:
 * - Injects values from application.yml
 * - Default values provided (for fallback)
 * 
 * @Bean:
 * - Creates RazorpayClient instance
 * - Spring manages lifecycle
 * - Singleton (one instance shared)
 */
@Configuration // Spring configuration class
public class RazorpayConfig {
    
    /**
     * RAZORPAY API KEY ID
     * 
     * Public key - Identifies merchant account
     * Used in frontend for Razorpay checkout
     * 
     * @Value: Injects from application.yml
     * Default: Test key (provided as fallback)
     */
    @Value("${razorpay.key.id:rzp_test_nwnTt5aqKf3z6f}")
    private String keyId;
    
    /**
     * RAZORPAY API SECRET
     * 
     * Private key - Authenticates server requests
     * Used for signature verification
     * NEVER expose to frontend!
     * 
     * SECURITY:
     * - Store in application.yml or environment variables
     * - Never commit to version control (in production)
     */
    @Value("${razorpay.key.secret:MWXd0wcJMMLWFJsl9crZo1lz}")
    private String keySecret;
    
    /**
     * RAZORPAY CLIENT BEAN
     * 
     * Creates and configures RazorpayClient
     * 
     * USAGE:
     * - Injected into RazorpayService
     * - Used for API calls (create order, verify payment)
     * 
     * SINGLETON:
     * - One instance shared across application
     * - Thread-safe (Razorpay SDK)
     * 
     * @return RazorpayClient - Configured Razorpay client
     * @throws RuntimeException - If initialization fails
     */
    @Bean
    public RazorpayClient razorpayClient() {
        try {
            // Initialize Razorpay client with API keys
            return new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            // If initialization fails, throw runtime exception
            throw new RuntimeException("Failed to initialize Razorpay client", e);
        }
    }
}
