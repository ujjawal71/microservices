package com.ecommerce.user.repository;

import com.ecommerce.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * USER REPOSITORY - Data Access Layer
 * 
 * ========================================================================
 * SPRING DATA JPA METHODS
 * ========================================================================
 * 
 * AUTOMATIC QUERY GENERATION:
 * - findByUsername() → "SELECT * FROM users WHERE username = ?"
 * - findByEmail() → "SELECT * FROM users WHERE email = ?"
 * - existsByUsername() → "SELECT COUNT(*) > 0 FROM users WHERE username = ?"
 * - existsByEmail() → "SELECT COUNT(*) > 0 FROM users WHERE email = ?"
 * 
 * ========================================================================
 * WHY OPTIONAL<T>
 * ========================================================================
 * 
 * NULL SAFETY:
 * - Optional prevents NullPointerException
 * - Explicit null handling
 * - Better than returning null
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * FIND BY USERNAME
     * 
     * Generated SQL: SELECT * FROM users WHERE username = ?
     * 
     * @param username - Username to search
     * @return Optional<User> - User if found, empty if not
     */
    Optional<User> findByUsername(String username);
    
    /**
     * FIND BY EMAIL
     * 
     * Generated SQL: SELECT * FROM users WHERE email = ?
     * 
     * @param email - Email to search
     * @return Optional<User> - User if found, empty if not
     */
    Optional<User> findByEmail(String email);
    
    /**
     * EXISTS BY USERNAME
     * 
     * Generated SQL: SELECT COUNT(*) > 0 FROM users WHERE username = ?
     * 
     * USE CASE:
     * - Check if username already exists during registration
     * - Faster than fetching full user object
     * 
     * @param username - Username to check
     * @return Boolean - true if exists, false otherwise
     */
    Boolean existsByUsername(String username);
    
    /**
     * EXISTS BY EMAIL
     * 
     * Generated SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     * 
     * USE CASE:
     * - Check if email already exists during registration
     * 
     * @param email - Email to check
     * @return Boolean - true if exists, false otherwise
     */
    Boolean existsByEmail(String email);
}
