package com.ecommerce.user.service;

import com.ecommerce.user.dto.RegisterRequest;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * USER SERVICE - User Management Business Logic
 * 
 * PURPOSE (उद्देश्य):
 * - User registration और management
 * - User data retrieval
 * - Password encoding (BCrypt)
 * 
 * SECURITY:
 * - Passwords hashed using BCrypt
 * - Never store plain passwords
 */
@Service
public class UserService {
    
    /**
     * USER REPOSITORY
     * Database operations के लिए
     */
    private final UserRepository userRepository;
    
    /**
     * PASSWORD ENCODER
     * BCrypt algorithm use करता है password hash करने के लिए
     */
    private final PasswordEncoder passwordEncoder;
    
    /**
     * CONSTRUCTOR INJECTION
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * REGISTER USER
     * 
     * New user registration handle करता है
     * 
     * FLOW:
     * 1. Check: Username already exists?
     * 2. Check: Email already exists?
     * 3. Password hash करो (BCrypt)
     * 4. User save करो (database)
     * 
     * SECURITY:
     * - Password hash करके store करता है (never plain text)
     * - BCrypt automatically salt add करता है
     * 
     * @param request - Registration request (username, email, password, etc.)
     * @return User - Created user object
     * @throws RuntimeException - If username/email already exists
     */
    public User registerUser(RegisterRequest request) {
        // Validate: Username must be unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Validate: Email must be unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // Hash password before storing (BCrypt)
        // Never store plain passwords!
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        // Save user to database
        return userRepository.save(user);
    }
    
    /**
     * FIND USER BY USERNAME
     * 
     * @param username - Username to search
     * @return User - User object
     * @throws RuntimeException - If user not found
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * FIND USER BY USERNAME OR EMAIL
     * 
     * Login के समय use होता है
     * User username या email से login कर सकता है
     * 
     * @param usernameOrEmail - Username or email
     * @return User - User object
     * @throws RuntimeException - If user not found
     */
    public User findByUsernameOrEmail(String usernameOrEmail) {
        // Try username first
        Optional<User> userByUsername = userRepository.findByUsername(usernameOrEmail);
        if (userByUsername.isPresent()) {
            return userByUsername.get();
        }
        
        // If not found, try email
        return userRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * FIND USER BY ID
     * 
     * @param id - User ID
     * @return User - User object
     * @throws RuntimeException - If user not found
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * GET ALL USERS
     * 
     * Admin के लिए सभी users fetch करता है
     * 
     * @return List<User> - All users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
