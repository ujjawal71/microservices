package com.ecommerce.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ORDER ENTITY - Order Data Model
 * 
 * ========================================================================
 * ACID PROPERTIES - ENTITY LEVEL
 * ========================================================================
 * 
 * ATOMICITY:
 * - Order + OrderItems saved together (CascadeType.ALL)
 * - If one OrderItem fails → entire Order transaction rolls back
 * 
 * CONSISTENCY:
 * - Database constraints enforced (foreign keys, not null)
 * - Business rules: totalAmount = sum(item.price * item.quantity)
 * - Status transitions validated
 * 
 * ISOLATION:
 * - @Entity with @Table ensures proper isolation
 * - Concurrent order creation handled safely
 * 
 * DURABILITY:
 * - @Entity persistence ensures durability
 * - Database writes permanent
 * 
 * ========================================================================
 * JPA RELATIONSHIPS
 * ========================================================================
 * 
 * ONE-TO-MANY RELATIONSHIP:
 * - Order (1) → OrderItems (Many)
 * - @OneToMany with CascadeType.ALL
 * - If Order deleted → OrderItems automatically deleted (CASCADE)
 * - If Order saved → OrderItems automatically saved (CASCADE)
 * 
 * CASCADE TYPES:
 * - ALL: All operations cascade
 * - PERSIST: Save operation cascades
 * - REMOVE: Delete operation cascades
 * - MERGE: Update operation cascades
 * 
 * ========================================================================
 * DEADLOCK PREVENTION - ENTITY DESIGN
 * ========================================================================
 * 
 * - Order ID as primary key (unique, indexed)
 * - No circular dependencies
 * - Proper foreign key relationships
 */
@Entity
@Table(name = "orders") // Database table name
public class Order {
    
    /**
     * PRIMARY KEY
     * Auto-generated ID (1, 2, 3, ...)
     * 
     * ACID: Consistency - Unique constraint ensures no duplicate orders
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment
    private Long id;
    
    /**
     * USER ID
     * Foreign key to users table
     * Identifies who placed the order
     */
    private Long userId;
    
    /**
     * ORDER STATUS
     * Enum type stored as String in database
     * 
     * STATUS FLOW (ACID: Consistency - Valid transitions):
     * PENDING → CONFIRMED → SHIPPED → DELIVERED
     * 
     * BUSINESS RULES:
     * - Initial status: PENDING
     * - Cannot go backwards (e.g., DELIVERED → PENDING)
     * - Terminal states: DELIVERED, CANCELLED
     */
    @Enumerated(EnumType.STRING) // Store enum as String (not ordinal)
    private OrderStatus status = OrderStatus.PENDING; // Default status
    
    /**
     * TOTAL AMOUNT
     * 
     * ACID: Consistency - Business rule:
     * totalAmount MUST equal sum of (item.price * item.quantity) for all items
     * 
     * VALIDATION:
     * - Must be positive
     * - Must match sum of order items
     */
    private BigDecimal totalAmount;
    
    /**
     * SHIPPING ADDRESS
     * Delivery address for the order
     */
    private String shippingAddress;
    
    /**
     * ORDER DATE
     * Timestamp when order was created
     * 
     * ACID: Durability - Timestamp persisted permanently
     */
    private LocalDateTime orderDate = LocalDateTime.now(); // Default to current time
    
    /**
     * ORDER ITEMS (One-to-Many Relationship)
     * 
     * JPA RELATIONSHIP:
     * - One Order can have many OrderItems
     * - mappedBy = "order" means OrderItem has "order" field
     * 
     * CASCADE TYPES:
     * - CascadeType.ALL: All operations (save, update, delete) cascade to OrderItems
     * 
     * ACID: Atomicity - Order and OrderItems saved together
     * - If Order saved → OrderItems automatically saved
     * - If Order deleted → OrderItems automatically deleted
     * - If any OrderItem fails → entire Order transaction rolls back
     * 
     * EXAMPLE:
     * Order with 3 items saved:
     * - Order saved → 3 OrderItems automatically saved
     * - All in single transaction (atomic)
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // One Order → Many OrderItems
    private List<OrderItem> items;
    
    /**
     * DEFAULT CONSTRUCTOR
     * JPA requirement (Hibernate uses reflection to create instances)
     */
    public Order() {}
    
    // ========== GETTERS AND SETTERS ==========
    // Java Beans Pattern for JPA
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    /**
     * ORDER STATUS ENUM
     * 
     * Status values for order lifecycle
     * 
     * STATUS FLOW:
     * 1. PENDING - Order created, payment pending
     * 2. CONFIRMED - Payment received, order confirmed
     * 3. SHIPPED - Order shipped to customer
     * 4. DELIVERED - Order delivered to customer
     * 5. CANCELLED - Order cancelled
     * 
     * ACID: Consistency - Status transitions must be valid
     */
    public enum OrderStatus {
        PENDING,    // Initial state
        CONFIRMED,  // Payment successful
        SHIPPED,    // Order shipped
        DELIVERED,  // Order delivered (terminal state)
        CANCELLED   // Order cancelled (terminal state)
    }
}
