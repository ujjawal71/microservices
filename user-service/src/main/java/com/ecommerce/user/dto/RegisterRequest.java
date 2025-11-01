package com.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REGISTER REQUEST DTO - User Registration Data Transfer Object
 * 
 * ========================================================================
 * VALIDATION CONSTRAINTS
 * ========================================================================
 * 
 * JAKARTA VALIDATION:
 * - @NotBlank: Field cannot be null, empty, or whitespace
 * - @Size: Length constraint (min/max)
 * - @Email: Email format validation
 * 
 * VALIDATION FLOW:
 * 1. Frontend sends request
 * 2. Controller receives request (@RequestBody)
 * 3. @Valid annotation triggers validation
 * 4. If validation fails → 400 Bad Request
 * 5. If validation passes → Proceed to service layer
 * 
 * ========================================================================
 * DTO PATTERN
 * ========================================================================
 * 
 * WHY DTO:
 * - Separate API contract from Entity
 * - Password field in DTO (input), but hashed in Entity
 * - Control what fields are exposed
 * - Prevent over-posting attacks
 */
public class RegisterRequest {
    
    /**
     * USERNAME
     * 
     * VALIDATION:
     * - @NotBlank: Cannot be empty
     * - @Size(min = 3, max = 50): Length between 3-50 characters
     * 
     * BUSINESS RULE:
     * - Must be unique (checked in service layer)
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    /**
     * EMAIL
     * 
     * VALIDATION:
     * - @NotBlank: Cannot be empty
     * - @Email: Must be valid email format
     * 
     * BUSINESS RULE:
     * - Must be unique (checked in service layer)
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    /**
     * PASSWORD
     * 
     * VALIDATION:
     * - @NotBlank: Cannot be empty
     * - @Size(min = 6): Minimum 6 characters
     * 
     * SECURITY:
     * - Plain password in DTO (input)
     * - Will be hashed (BCrypt) in service layer
     * - Never store plain password!
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    /**
     * FIRST NAME
     * Optional field
     */
    private String firstName;
    
    /**
     * LAST NAME
     * Optional field
     */
    private String lastName;
    
    /**
     * DEFAULT CONSTRUCTOR
     * Required for JSON deserialization
     */
    public RegisterRequest() {}
    
    // ========== GETTERS AND SETTERS ==========
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
