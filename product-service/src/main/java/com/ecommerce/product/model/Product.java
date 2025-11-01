package com.ecommerce.product.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * PRODUCT ENTITY - Product Data Model
 * 
 * ========================================================================
 * ACID PROPERTIES - ENTITY LEVEL
 * ========================================================================
 * 
 * ATOMICITY:
 * - Product save is atomic
 * - All product fields saved together
 * 
 * CONSISTENCY:
 * - Validation constraints enforced (@NotBlank, @DecimalMin)
 * - Price must be positive
 * - Name must not be blank
 * 
 * ISOLATION:
 * - Concurrent product updates handled safely
 * - Product Service manages product data
 * 
 * DURABILITY:
 * - Product data persisted permanently
 * 
 * ========================================================================
 * VALIDATION CONSTRAINTS
 * ========================================================================
 * 
 * JAKARTA VALIDATION:
 * - @NotBlank: Field cannot be null or empty
 * - @DecimalMin: Minimum value constraint
 * - Validation enforced at service/controller layer
 * 
 * ========================================================================
 * MICROSERVICES CONCEPT
 * ========================================================================
 * 
 * SEPARATION OF CONCERNS:
 * - Product data in Product Service
 * - Order Service stores product snapshot (denormalization)
 * - Inventory Service manages stock (separate concern)
 */
@Entity
@Table(name = "products") // Database table name
public class Product {
    
    /**
     * PRIMARY KEY
     * Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * PRODUCT NAME
     * 
     * VALIDATION:
     * - @NotBlank: Cannot be null or empty
     * - Must have value
     * 
     * ACID: Consistency - Name required for product
     */
    @NotBlank // Validation: Cannot be blank
    private String name;
    
    /**
     * PRODUCT DESCRIPTION
     * Detailed description of the product
     */
    private String description;
    
    /**
     * PRODUCT PRICE
     * 
     * VALIDATION:
     * - @DecimalMin(value = "0.0", inclusive = false): Must be greater than 0
     * - Price cannot be zero or negative
     * 
     * ACID: Consistency - Price must be positive
     */
    @DecimalMin(value = "0.0", inclusive = false) // Must be > 0
    private BigDecimal price;
    
    /**
     * PRODUCT CATEGORY
     * Category classification (e.g., "Electronics", "Clothing")
     */
    private String category;
    
    /**
     * IMAGE URL
     * URL to product image
     */
    private String imageUrl;
    
    /**
     * STOCK QUANTITY
     * 
     * STOCK MANAGEMENT:
     * - Default value: 10 (set in code when creating product via API)
     * - NOT set as database default (only set when product is created)
     * - Represents available stock quantity
     * - Value 0 or less means OUT OF STOCK
     * 
     * NOTE: This is denormalized data
     * Actual stock managed by Inventory Service
     * This field may be used for quick reference or legacy support
     * 
     * VALIDATION:
     * - Cannot be negative
     * - ACID: Consistency - Stock quantity must be >= 0
     * - Can be NULL for existing products (not yet updated)
     */
    @Column(name = "stock_quantity")
    private Integer stockQuantity; // No database default - set in code only
    
    /**
     * DEFAULT CONSTRUCTOR
     * JPA requirement
     */
    public Product() {}
    
    // ========== GETTERS AND SETTERS ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { 
        // ACID: Consistency - Stock cannot be negative
        if (stockQuantity != null && stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.stockQuantity = stockQuantity; 
    }
    
    /**
     * IS IN STOCK - Check if product is available
     * 
     * BUSINESS RULE:
     * - Product is in stock if stockQuantity > 0
     * - Product is out of stock if stockQuantity <= 0
     * 
     * USE CASE:
     * - Frontend can disable "Add to Cart" button if out of stock
     * - Display "Out of Stock" message
     * - Filter products by stock availability
     * 
     * @return boolean - true if product is in stock (stockQuantity > 0), false otherwise
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
    
    /**
     * IS OUT OF STOCK - Check if product is unavailable
     * 
     * @return boolean - true if product is out of stock (stockQuantity <= 0), false otherwise
     */
    public boolean isOutOfStock() {
        return !isInStock(); // Inverse of isInStock()
    }
    
    /**
     * GET STOCK STATUS - Human-readable stock status
     * 
     * @return String - "In Stock" or "Out of Stock"
     */
    public String getStockStatus() {
        return isInStock() ? "In Stock" : "Out of Stock";
    }
}
