package com.ecommerce.user.service;

import com.ecommerce.user.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AUTH SERVICE - Authentication Business Logic
 * 
 * PURPOSE (उद्देश्य):
 * - User authentication handle करना (login)
 * - JWT tokens generate करना
 * - Password verification करना
 * 
 * RESPONSIBILITY:
 * - Login verification
 * - Token generation
 * - Password matching
 * 
 * DEPENDENCIES:
 * - UserService: User data fetch करने के लिए
 * - PasswordEncoder: Password hash/verify करने के लिए (BCrypt)
 * - JwtUtil: Token generate करने के लिए
 * 
 * SECURITY:
 * - Passwords hashed हैं (BCrypt algorithm)
 * - Plain password database में store नहीं होता
 * - Password verification secure तरीके से होती है
 */
@Service // Spring service component
public class AuthService {
    
    /**
     * USER SERVICE DEPENDENCY
     * User data fetch करने के लिए (find user by username/email)
     */
    private final UserService userService;
    
    /**
     * PASSWORD ENCODER DEPENDENCY
     * BCrypt algorithm use करता है password hash/verify के लिए
     * 
     * BCrypt:
     * - One-way hashing algorithm
     * - Same password = different hash (salt added)
     * - Cannot reverse (secure)
     */
    private final PasswordEncoder passwordEncoder;
    
    /**
     * JWT UTILITY DEPENDENCY
     * Token generate और validate करने के लिए
     */
    private final JwtUtil jwtUtil;
    
    /**
     * CONSTRUCTOR INJECTION
     * Spring automatically dependencies inject करता है
     */
    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * AUTHENTICATE USER
     * 
     * Login के समय call होता है
     * Username/Email और Password verify करता है
     * 
     * FLOW:
     * 1. Username/Email से user ढूंढो
     * 2. Plain password को stored hash से match करो
     * 3. Match होने पर JWT token generate करो
     * 4. Token return करो
     * 
     * @param usernameOrEmail - User का username या email
     * @param password - Plain text password (user input)
     * @return String - JWT token (if authentication successful)
     * @throws RuntimeException - If credentials invalid
     * 
     * SECURITY FLOW:
     * User Input → Plain Password
     * Database → Hashed Password (BCrypt)
     * passwordEncoder.matches() → Compare both
     * 
     * WHY NOT COMPARE DIRECTLY:
     * - Passwords hashed हैं (cannot decrypt)
     * - BCrypt same password = different hash (salt)
     * - matches() method internally salt handle करता है
     */
    public String authenticate(String usernameOrEmail, String password) {
        // Find user by username or email
        var user = userService.findByUsernameOrEmail(usernameOrEmail);
        
        // Verify password (compare plain password with hashed password)
        // passwordEncoder.matches() handles BCrypt salt automatically
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        // If password matches, generate and return JWT token
        return generateToken(user.getUsername());
    }
    
    /**
     * GENERATE TOKEN
     * 
     * JWT token generate करता है username के लिए
     * 
     * @param username - User का username
     * @return String - JWT token
     */
    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }
    
    /**
     * EXTRACT USERNAME FROM TOKEN
     * 
     * JWT token से username extract करता है
     * 
     * USE CASE:
     * - /api/auth/me endpoint में current user जानने के लिए
     * - Token से user identify करना
     * 
     * @param token - JWT token string
     * @return String - Username extracted from token
     */
    public String extractUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }
}
