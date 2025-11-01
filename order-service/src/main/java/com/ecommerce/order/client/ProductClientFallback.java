package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * PRODUCT CLIENT FALLBACK - Circuit Breaker Fallback Implementation
 * 
 * PURPOSE (उद्देश्य):
 * - Product Service unavailable होने पर fallback response
 * - Circuit Breaker pattern का part
 * - Graceful degradation
 * 
 * CIRCUIT BREAKER FALLBACK:
 * - Product Service down होने पर call होता है
 * - Default/error response return करता है
 * - Order creation fail नहीं होता (graceful handling)
 * 
 * WHY NEEDED:
 * - Product Service might be down
 * - Network issues
 * - Service overload
 * - Instead of crashing → Return default value
 */
@Component // Spring component (auto-detected)
public class ProductClientFallback implements ProductClient {
    
    /**
     * FALLBACK METHOD
     * 
     * Product Service unavailable होने पर call होता है
     * 
     * BEHAVIOR:
     * - Returns default ProductDto
     * - Name indicates unavailability
     * - Price = 0, Stock = 0
     * - Order creation can continue (with default values)
     * 
     * @param id - Product ID (requested product)
     * @return ProductDto - Fallback product (indicates service unavailable)
     */
    @Override
    public ProductDto getProduct(Long id) {
        // Fallback response when product-service is unavailable
        ProductDto fallback = new ProductDto();
        fallback.setId(id); // Keep requested ID
        fallback.setName("Product temporarily unavailable"); // Indicate unavailability
        fallback.setPrice(BigDecimal.ZERO); // Zero price
        fallback.setStockQuantity(0); // Zero stock
        
        // Log fallback activation (for monitoring)
        System.err.println("Circuit breaker fallback activated for product: " + id);
        
        return fallback;
    }
}
