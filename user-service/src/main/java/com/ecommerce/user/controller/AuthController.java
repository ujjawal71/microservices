package com.ecommerce.user.controller;

import com.ecommerce.user.dto.AuthResponse;
import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.RegisterRequest;
import com.ecommerce.user.model.User;
import com.ecommerce.user.service.AuthService;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AUTH CONTROLLER - Authentication REST API
 * 
 * PURPOSE (उद्देश्य):
 * - User registration और login endpoints
 * - JWT token generation
 * - Current user information
 * 
 * REST API ENDPOINTS:
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - User login
 * - GET /api/auth/me - Get current user (using JWT token)
 * - GET /api/auth/users - Get all users (admin)
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    /**
     * AUTH SERVICE
     * Authentication logic के लिए
     */
    private final AuthService authService;
    
    /**
     * USER SERVICE
     * User data operations के लिए
     */
    private final UserService userService;
    
    /**
     * CONSTRUCTOR INJECTION
     */
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }
    
    /**
     * REGISTER ENDPOINT
     * 
     * New user registration handle करता है
     * 
     * FLOW:
     * 1. Validate request (@Valid annotation)
     * 2. Register user (UserService)
     * 3. Generate JWT token (AuthService)
     * 4. Return token + user info
     * 
     * @Valid: Request validation (email format, password length, etc.)
     * 
     * @param request - Registration request
     * @return ResponseEntity - JWT token + user info (200 OK) or error (400 Bad Request)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Register user (password will be hashed automatically)
            User user = userService.registerUser(request);
            
            // Generate JWT token for immediate login
            String token = authService.generateToken(user.getUsername());
            
            // Return token + user info (including role)
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), 
                    user.getUsername(), user.getEmail(), user.getRole()));
        } catch (RuntimeException e) {
            // Registration failed (username/email already exists)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * LOGIN ENDPOINT
     * 
     * User login handle करता है
     * 
     * FLOW:
     * 1. Validate request
     * 2. Authenticate user (verify password)
     * 3. Generate JWT token
     * 4. Return token + user info
     * 
     * @param request - Login request (username/email + password)
     * @return ResponseEntity - JWT token + user info (200 OK) or error (401 Unauthorized)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user (verify password)
            // Returns JWT token if credentials valid
            String token = authService.authenticate(request.getUsername(), request.getPassword());
            
            // Fetch user details
            User user = userService.findByUsernameOrEmail(request.getUsername());
            
            // Return token + user info (including role)
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), 
                    user.getUsername(), user.getEmail(), user.getRole()));
        } catch (RuntimeException e) {
            // Authentication failed (invalid credentials)
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    
    /**
     * GET CURRENT USER ENDPOINT
     * 
     * JWT token से current user information fetch करता है
     * 
     * USE CASE:
     * - Frontend page refresh के बाद user info restore करने के लिए
     * - Check if user is logged in
     * - Get user role for authorization
     * 
     * FLOW:
     * 1. Extract JWT token from Authorization header
     * 2. Validate token format (Bearer token)
     * 3. Extract username from token
     * 4. Fetch user from database
     * 5. Return user info
     * 
     * @param authHeader - Authorization header (Bearer <token>)
     * @return ResponseEntity - User info (200 OK) or error (401 Unauthorized)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Check if Authorization header present and valid format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            // Extract token (remove "Bearer " prefix)
            String token = authHeader.substring(7);
            
            // Extract username from JWT token
            String username = authService.extractUsernameFromToken(token);
            
            // Fetch user from database
            User user = userService.findByUsername(username);
            
            // Return user info with token
            AuthResponse response = new AuthResponse(token, user.getId(), 
                    user.getUsername(), user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Token invalid or expired
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
    
    /**
     * GET ALL USERS ENDPOINT
     * 
     * Admin के लिए सभी users fetch करता है
     * 
     * @return ResponseEntity - List of all users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
