package com.ecommerce.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * ORDER ITEM ENTITY - Order Item Data Model
 * 
 * ========================================================================
 * ACID PROPERTIES - ENTITY LEVEL
 * ========================================================================
 * 
 * ATOMICITY:
 * - Saved as part of Order transaction
 * - If Order fails → OrderItem not saved (cascade)
 * 
 * CONSISTENCY:
 * - Foreign key to Order (order_id)
 * - Business rules: quantity > 0, price > 0
 * 
 * ISOLATION:
 * - Entity-level isolation through JPA
 * 
 * DURABILITY:
 * - Persisted with Order
 * 
 * ========================================================================
 * JPA RELATIONSHIPS
 * ========================================================================
 * 
 * MANY-TO-ONE RELATIONSHIP:
 * - Many OrderItems → One Order
 * - @ManyToOne with @JoinColumn
 * - Foreign key: order_id in order_items table
 * 
 * DENORMALIZATION:
 * - productName and price stored (even though they exist in Product table)
 * - Reason: Product price/name may change, but order should preserve original values
 * - ACID: Consistency - Historical data preserved
 * 
 * ========================================================================
 * JSON SERIALIZATION
 * ========================================================================
 * 
 * CIRCULAR REFERENCE PREVENTION:
 * - Order → OrderItems → Order (circular!)
 * - @JsonIgnore on "order" field prevents infinite loop
 * - OrderItem JSON doesn't include full Order object
 */
@Entity
@Table(name = "order_items") // Database table name
public class OrderItem {
    
    /**
     * PRIMARY KEY
     * Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ORDER (Many-to-One Relationship)
     * 
     * JPA RELATIONSHIP:
     * - Many OrderItems belong to One Order
     * - @JoinColumn creates foreign key "order_id" in order_items table
     * 
     * CASCADE:
     * - OrderItem operations do NOT cascade to Order
     * - Order is parent, OrderItem is child
     * 
     * @JsonIgnore:
     * - Prevents circular reference in JSON
     * - Order → OrderItems → Order (infinite loop!)
     * - Ignoring "order" field in JSON breaks the cycle
     */
    @ManyToOne // Many OrderItems → One Order
    @JoinColumn(name = "order_id") // Foreign key column name
    @JsonIgnore  // Prevent circular reference in JSON serialization
    private Order order;
    
    /**
     * PRODUCT ID
     * Reference to Product (not a JPA relationship, just ID)
     * 
     * WHY NOT @ManyToOne to Product?
     * - Product is in different service (Product Service)
     * - Microservices don't share entities
     * - Only store ID for reference
     */
    private Long productId;
    
    /**
     * PRODUCT NAME (DENORMALIZATION)
     * 
     * DENORMALIZATION CONCEPT:
     * - Product name stored here (even though it's in Product table)
     * - Reason: Product name may change in future
     * - Order should preserve original product name at time of purchase
     * 
     * ACID: Consistency - Historical data preserved
     * - "iPhone 15" renamed to "iPhone 15 Pro"
     * - Old orders still show "iPhone 15" (correct historical data)
     */
    private String productName;
    
    /**
     * QUANTITY
     * 
     * ACID: Consistency - Must be positive
     * Business rule: quantity > 0
     */
    private Integer quantity;
    
    /**
     * PRICE (DENORMALIZATION)
     * 
     * PRICE SNAPSHOT:
     * - Product price stored at time of order
     * - Product price may change later
     * - Order preserves original price
     * 
     * EXAMPLE:
     * - Product price: ₹50000 (at order time)
     * - Product price changed to: ₹45000 (later)
     * - Order still shows: ₹50000 (correct historical price)
     * 
     * ACID: Consistency - Historical data integrity
     */
    private BigDecimal price;
    
    /**
     * DEFAULT CONSTRUCTOR
     * JPA requirement
     */
    public OrderItem() {}
    
    // ========== GETTERS AND SETTERS ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
