package com.ecommerce.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT UTILITY - JSON Web Token Helper Class
 * 
 * PURPOSE (उद्देश्य):
 * - JWT tokens generate करना
 * - JWT tokens validate करना
 * - JWT tokens से user information extract करना
 * 
 * JWT CONCEPT (JSON Web Token):
 * - JWT = Stateless authentication token
 * - User login करता है → Token मिलता है
 * - हर request में token भेजता है → Server validate करता है
 * - No need to store sessions in database! ✅
 * 
 * JWT STRUCTURE:
 * Header.Payload.Signature
 * 
 * Example: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTYzODM2NzYwMCwiZXhwIjoxNjM4NDU0MDAwfQ.signature
 * 
 * WHY JWT:
 * - Stateless: Server memory में session store नहीं करना पड़ता
 * - Scalable: Multiple servers share कर सकते हैं (same secret key)
 * - Secure: Signed with secret key (tampering detect हो जाता है)
 * - Self-contained: Token में user info होता है (no database lookup needed)
 * 
 * SECURITY:
 * - Secret key से sign होता है (application.yml में store)
 * - Expiration time set होता है (auto logout after expiry)
 * - HMAC SHA algorithm use होता है (secure)
 */
@Component // Spring component: Auto-detected and registered as bean
public class JwtUtil {
    
    /**
     * JWT SECRET KEY
     * 
     * @Value annotation: application.yml से value inject होती है
     * Format: ${jwt.secret} = application.yml में defined key
     * 
     * SECURITY NOTE:
     * - Production में strong random secret use करें
     * - Never commit secret to version control
     * - Use environment variables or secret management tools
     */
    @Value("${jwt.secret}") // Inject from application.yml
    private String secret;
    
    /**
     * JWT EXPIRATION TIME
     * 
     * Token कितने समय तक valid रहेगा (milliseconds में)
     * Example: 3600000 = 1 hour
     * 
     * After expiration:
     * - Token invalid हो जाता है
     * - User को फिर से login करना पड़ता है
     */
    @Value("${jwt.expiration}") // Inject from application.yml
    private Long expiration;
    
    /**
     * GET SIGNING KEY
     * 
     * Secret key को JWT library के format में convert करता है
     * HMAC SHA algorithm के लिए key prepare करता है
     * 
     * @return SecretKey - JWT signing के लिए key
     */
    private SecretKey getSigningKey() {
        // Convert string secret to cryptographic key
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * GENERATE TOKEN
     * 
     * User login करने पर यह method call होता है
     * Username को token में embed करता है
     * 
     * FLOW:
     * 1. Current time और expiry time set करो
     * 2. Username को subject में add करो
     * 3. Secret key से sign करो
     * 4. Compact string format में return करो
     * 
     * @param username - User का username (token में store होगा)
     * @return String - JWT token (compact string format)
     * 
     * TOKEN CONTENT:
     * - Subject (username)
     * - Issued At (creation time)
     * - Expiration (expiry time)
     * - Signature (secret key से signed)
     * 
     * EXAMPLE:
     * Input: username = "user1"
     * Output: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     */
    public String generateToken(String username) {
        Date now = new Date(); // Current time
        Date expiryDate = new Date(now.getTime() + expiration); // Current time + expiration
        
        // Build JWT token
        return Jwts.builder()
                .subject(username) // Set username as subject
                .issuedAt(now) // Set creation time
                .expiration(expiryDate) // Set expiry time
                .signWith(getSigningKey()) // Sign with secret key
                .compact(); // Convert to compact string
    }
    
    /**
     * GET USERNAME FROM TOKEN
     * 
     * JWT token से username extract करता है
     * Token validate भी करता है (invalid token → exception)
     * 
     * USE CASE:
     * - /api/auth/me endpoint में current user जानने के लिए
     * - Authorization check करने के लिए
     * 
     * FLOW:
     * 1. Token को parse करो
     * 2. Signature verify करो (secret key से)
     * 3. Expiration check करो
     * 4. Subject (username) extract करो
     * 
     * @param token - JWT token string
     * @return String - Username extracted from token
     * @throws Exception - If token is invalid, expired, or tampered
     * 
     * SECURITY:
     * - Signature verification: Token modify नहीं हो सकता
     * - Expiration check: Expired tokens reject होते हैं
     */
    public String getUsernameFromToken(String token) {
        // Parse and verify token
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey()) // Verify signature
                .build()
                .parseSignedClaims(token) // Parse token
                .getPayload(); // Get token payload (contains claims)
        
        return claims.getSubject(); // Extract username from subject claim
    }
    
    /**
     * VALIDATE TOKEN
     * 
     * Token valid है या नहीं check करता है
     * 
     * CHECKS:
     * - Token format correct है या नहीं
     * - Signature valid है या नहीं
     * - Expired है या नहीं
     * - Tampered है या नहीं
     * 
     * @param token - JWT token string
     * @return boolean - true if valid, false if invalid
     * 
     * USE CASE:
     * - API endpoints पर authorization check करने के लिए
     * - Token refresh करने से पहले validate करना
     */
    public boolean validateToken(String token) {
        try {
            // Try to parse and verify token
            Jwts.parser()
                .verifyWith(getSigningKey()) // Verify signature
                .build()
                .parseSignedClaims(token); // Parse token
            return true; // Token is valid
        } catch (Exception e) {
            // Token is invalid (expired, tampered, or malformed)
            return false;
        }
    }
}
