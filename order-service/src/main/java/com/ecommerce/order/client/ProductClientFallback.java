package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductClientFallback implements ProductClient {
    
    @Override
    public ProductDto getProduct(Long id) {
        // Fallback response when product-service is unavailable
        ProductDto fallback = new ProductDto();
        fallback.setId(id);
        fallback.setName("Product temporarily unavailable");
        fallback.setPrice(BigDecimal.ZERO);
        fallback.setStockQuantity(0);
        return fallback;
    }
}

