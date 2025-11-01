package com.ecommerce.order.dto;

import java.math.BigDecimal;

/**
 * PRODUCT DTO - Product Data Transfer Object
 * 
 * ========================================================================
 * INTER-SERVICE COMMUNICATION DTO
 * ========================================================================
 * 
 * PURPOSE:
 * - Used for communication between Order Service and Product Service
 * - Order Service receives ProductDto from Product Service (Feign Client)
 * 
 * MICROSERVICES CONCEPT:
 * - Services don't share entities (different databases)
 * - DTOs used for data transfer
 * - Order Service doesn't have Product entity
 * - Only receives ProductDto from Product Service
 * 
 * WHY DTO:
 * - Loose coupling between services
 * - Services can evolve independently
 * - API contract (not internal structure)
 */
public class ProductDto {
    
    /**
     * PRODUCT ID
     */
    private Long id;
    
    /**
     * PRODUCT NAME
     * Used to populate OrderItem.productName (denormalization)
     */
    private String name;
    
    /**
     * PRODUCT PRICE
     * Used to populate OrderItem.price (price snapshot)
     */
    private BigDecimal price;
    
    /**
     * STOCK QUANTITY
     * May be used for validation (though actual stock in Inventory Service)
     */
    private Integer stockQuantity;
    
    /**
     * DEFAULT CONSTRUCTOR
     * Required for JSON deserialization (Feign Client)
     */
    public ProductDto() {}
    
    // ========== GETTERS AND SETTERS ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
