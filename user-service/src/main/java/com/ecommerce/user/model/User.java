package com.ecommerce.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * USER ENTITY - User Data Model
 * 
 * PURPOSE (उद्देश्य):
 * - User information store करना
 * - Authentication और authorization के लिए
 * 
 * ROLE-BASED ACCESS CONTROL (RBAC):
 * - role field: USER or ADMIN
 * - Default role: USER
 * - Admin panel access के लिए ADMIN role required
 */
@Entity
@Table(name = "users")
public class User {
    
    /**
     * PRIMARY KEY
     * Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * USERNAME
     * Unique identifier for login
     * 
     * VALIDATION:
     * - @NotBlank: Cannot be empty
     * - @Size(min = 3, max = 50): Length constraint
     * - @Column(unique = true): Database unique constraint
     */
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;
    
    /**
     * EMAIL
     * Unique email address
     * 
     * VALIDATION:
     * - @NotBlank: Cannot be empty
     * - @Email: Must be valid email format
     * - @Column(unique = true): Database unique constraint
     */
    @NotBlank
    @Email
    @Column(unique = true)
    private String email;
    
    /**
     * PASSWORD
     * Hashed password (BCrypt)
     * 
     * SECURITY:
     * - Never store plain passwords!
     * - Always hashed before storing
     * - BCrypt algorithm use होता है
     * 
     * VALIDATION:
     * - @NotBlank: Cannot be empty
     * - @Size(min = 6): Minimum 6 characters
     */
    @NotBlank
    @Size(min = 6)
    private String password;
    
    /**
     * FIRST NAME
     * User's first name
     */
    private String firstName;
    
    /**
     * LAST NAME
     * User's last name
     */
    private String lastName;
    
    /**
     * PHONE
     * Contact phone number
     */
    private String phone;
    
    /**
     * ADDRESS
     * Shipping address
     */
    private String address;
    
    /**
     * ROLE
     * User role for authorization (RBAC)
     * 
     * VALUES:
     * - "USER": Regular user (default)
     * - "ADMIN": Administrator (access to admin panel)
     * 
     * DEFAULT: "USER"
     */
    private String role = "USER";
    
    /**
     * DEFAULT CONSTRUCTOR
     * JPA requirement
     */
    public User() {}
    
    /**
     * PARAMETERIZED CONSTRUCTOR
     * Convenience constructor
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
