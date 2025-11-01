package com.ecommerce.order.dto;

import java.util.List;

/**
 * ORDER REQUEST DTO - Data Transfer Object
 * 
 * ========================================================================
 * DTO PATTERN
 * ========================================================================
 * 
 * PURPOSE:
 * - Data transfer between client (frontend) and server
 * - Separates API contract from internal entity structure
 * - Validation at API boundary
 * 
 * WHY DTO:
 * - Entity classes may have internal fields not exposed
 * - DTOs provide clean API contract
 * - Validation at DTO level
 * - Prevents over-posting attacks
 * 
 * ========================================================================
 * DATA VALIDATION
 * ========================================================================
 * 
 * VALIDATION CONCERNS:
 * - userId: Required, must exist
 * - shippingAddress: Required
 * - items: Required, at least one item
 * - quantity: Must be positive
 * 
 * VALIDATION HAPPENS AT:
 * - Controller level (@Valid annotation)
 * - Before business logic execution
 */
public class OrderRequest {
    
    /**
     * USER ID
     * ID of user placing the order
     */
    private Long userId;
    
    /**
     * SHIPPING ADDRESS
     * Delivery address for the order
     */
    private String shippingAddress;
    
    /**
     * ORDER ITEMS
     * List of products in the order
     * 
     * Each item contains:
     * - productId: Product being ordered
     * - quantity: How many units
     */
    private List<OrderItemDto> items;
    
    /**
     * DEFAULT CONSTRUCTOR
     * Required for JSON deserialization
     */
    public OrderRequest() {}
    
    // ========== GETTERS AND SETTERS ==========
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }
    
    /**
     * ORDER ITEM DTO (Nested Class)
     * 
     * Represents a single item in the order
     * 
     * FIELDS:
     * - productId: Product identifier
     * - quantity: Number of units
     */
    public static class OrderItemDto {
        
        /**
         * PRODUCT ID
         * Reference to product being ordered
         */
        private Long productId;
        
        /**
         * QUANTITY
         * Number of units of this product
         * 
         * VALIDATION:
         * - Must be positive (> 0)
         * - Business rule enforced in service layer
         */
        private Integer quantity;
        
        /**
         * DEFAULT CONSTRUCTOR
         */
        public OrderItemDto() {}
        
        // ========== GETTERS AND SETTERS ==========
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
