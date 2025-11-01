package com.ecommerce.user.service;

import com.ecommerce.user.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    public String authenticate(String usernameOrEmail, String password) {
        var user = userService.findByUsernameOrEmail(usernameOrEmail);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return generateToken(user.getUsername());
    }
    
    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }
    
    public String extractUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }
}

