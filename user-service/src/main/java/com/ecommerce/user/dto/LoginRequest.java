package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * LOGIN REQUEST DTO - User Login Data Transfer Object
 * 
 * ========================================================================
 * AUTHENTICATION DTO
 * ========================================================================
 * 
 * PURPOSE:
 * - User login credentials
 * - Username/Email and password
 * 
 * VALIDATION:
 * - @NotBlank: Both fields required
 * - Username can be username or email
 * 
 * SECURITY:
 * - Password is plain text (input from user)
 * - Will be verified against hashed password in database
 * - Never log passwords!
 */
public class LoginRequest {
    
    /**
     * USERNAME OR EMAIL
     * 
     * User can login with either username or email
     * Service layer handles both cases
     * 
     * VALIDATION:
     * - @NotBlank: Cannot be empty
     */
    @NotBlank(message = "Username or email is required")
    private String username;
    
    /**
     * PASSWORD
     * 
     * Plain text password from user input
     * Will be verified against BCrypt hash in database
     * 
     * VALIDATION:
     * - @NotBlank: Cannot be empty
     * 
     * SECURITY:
     * - Never log this value
     * - Never return in response
     */
    @NotBlank(message = "Password is required")
    private String password;
    
    /**
     * DEFAULT CONSTRUCTOR
     */
    public LoginRequest() {}
    
    // ========== GETTERS AND SETTERS ==========
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
