package com.ecommerce.order.client;

import org.springframework.stereotype.Component;

/**
 * INVENTORY CLIENT FALLBACK - Circuit Breaker Fallback
 * 
 * PURPOSE:
 * - Inventory Service unavailable होने पर call होता है
 * - Circuit breaker pattern का part है
 * 
 * BEHAVIOR:
 * - Returns false (insufficient stock)
 * - Prevents order creation when inventory service is down
 * - Better to fail safe (don't oversell)
 */
@Component
public class InventoryClientFallback implements InventoryClient {
    
    @Override
    public Boolean reserveInventory(Long productId, Integer quantity) {
        // Fallback: Return false (insufficient stock)
        // Better to fail safe - don't create orders when inventory service is down
        System.err.println("Inventory Service unavailable - Cannot reserve stock. Product ID: " + productId);
        return false;
    }
    
    @Override
    public Object deductInventory(Long productId, Integer quantity) {
        // Fallback: Log error but don't fail order
        // Stock deduction can be retried later if inventory service recovers
        System.err.println("Inventory Service unavailable - Cannot deduct stock. Product ID: " + productId + ", Quantity: " + quantity);
        return null;
    }
}

