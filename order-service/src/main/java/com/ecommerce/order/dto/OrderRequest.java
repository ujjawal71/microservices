package com.ecommerce.order.dto;

import java.util.List;

public class OrderRequest {
    private Long userId;
    private String shippingAddress;
    private List<OrderItemDto> items;
    
    public OrderRequest() {}
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }
    
    public static class OrderItemDto {
        private Long productId;
        private Integer quantity;
        
        public OrderItemDto() {}
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}

