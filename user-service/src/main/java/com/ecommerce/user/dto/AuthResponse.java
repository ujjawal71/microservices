package com.ecommerce.user.dto;

/**
 * AUTH RESPONSE DTO - Authentication Response Data Transfer Object
 * 
 * ========================================================================
 * RESPONSE DTO PATTERN
 * ========================================================================
 * 
 * PURPOSE:
 * - Structured response for login/register endpoints
 * - Contains authentication token and user information
 * - Clean API contract
 * 
 * JWT TOKEN RESPONSE:
 * - token: JWT token for authentication
 * - type: Token type (Bearer)
 * - User information: id, username, email, role
 * 
 * ========================================================================
 * SECURITY CONSIDERATIONS
 * ========================================================================
 * 
 * SENSITIVE DATA:
 * - Password NEVER included in response
 * - Only safe user information returned
 * 
 * TOKEN TYPE:
 * - "Bearer" indicates token-based authentication
 * - Frontend uses: "Authorization: Bearer <token>"
 */
public class AuthResponse {
    
    /**
     * JWT TOKEN
     * 
     * Authentication token for subsequent requests
     * 
     * USAGE:
     * - Frontend stores token (localStorage)
     * - Includes in Authorization header: "Bearer <token>"
     * - Server validates token on each request
     */
    private String token;
    
    /**
     * TOKEN TYPE
     * 
     * Default: "Bearer"
     * Indicates token-based authentication scheme
     */
    private String type = "Bearer";
    
    /**
     * USER ID
     * User's unique identifier
     */
    private Long id;
    
    /**
     * USERNAME
     * User's username
     */
    private String username;
    
    /**
     * EMAIL
     * User's email address
     */
    private String email;
    
    /**
     * USER ROLE
     * 
     * Role-based access control (RBAC)
     * Values: "USER" or "ADMIN"
     * 
     * USAGE:
     * - Frontend uses role for authorization
     * - Admin panel access requires "ADMIN" role
     */
    private String role;
    
    /**
     * DEFAULT CONSTRUCTOR
     * Required for JSON serialization
     */
    public AuthResponse() {}
    
    /**
     * PARAMETERIZED CONSTRUCTOR
     * 
     * @param token - JWT token
     * @param id - User ID
     * @param username - Username
     * @param email - Email
     * @param role - User role (USER or ADMIN)
     */
    public AuthResponse(String token, Long id, String username, String email, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
