package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * PRODUCT CLIENT - Feign Client for Inter-Service Communication
 * 
 * PURPOSE (उद्देश्य):
 * - Order Service से Product Service को call करना
 * - Product information fetch करना (order create करने के लिए)
 * - Inter-service communication handle करना
 * 
 * FEIGN CLIENT CONCEPT:
 * - Feign = Declarative HTTP client (Spring Cloud)
 * - Interface define करते हैं, Spring automatically implementation generate करता है
 * - No HTTP client code needed! ✅
 * - Automatic service discovery integration
 * 
 * MICROSERVICES COMMUNICATION:
 * Order Service → (HTTP Call) → Product Service
 * 
 * WITHOUT FEIGN (Traditional):
 * - Manual RestTemplate/WebClient code
 * - URL hardcoding
 * - Error handling manually
 * - Load balancing manually
 * 
 * WITH FEIGN (Modern):
 * - Just define interface
 * - Spring handles everything automatically
 * - Service discovery integration
 * - Load balancing built-in
 * - Circuit breaker integration
 * 
 * SERVICE DISCOVERY:
 * - Eureka server पर services register होते हैं
 * - Feign automatically service name resolve करता है
 * - Example: "product-service" → http://product-service:8082/api/products/{id}
 * 
 * FALLBACK CONCEPT:
 * - Circuit breaker pattern
 * - If Product Service down → fallback method call होता है
 * - Prevents cascading failures
 * - Returns default/error response
 */
@FeignClient(
    name = "product-service", // Service name in Eureka (service discovery)
    fallback = ProductClientFallback.class, // Fallback class if service unavailable
    url = "" // Empty = Use service discovery (Eureka)
)
public interface ProductClient {
    
    /**
     * GET PRODUCT BY ID
     * 
     * Order Service को product information चाहिए order create करने के लिए
     * यह method Product Service को HTTP call करता है
     * 
     * HOW IT WORKS:
     * 1. Order Service calls: productClient.getProduct(1L)
     * 2. Feign intercepts the call
     * 3. Feign resolves "product-service" from Eureka
     * 4. Makes HTTP GET request: http://product-service:8082/api/products/1
     * 5. Receives ProductDto response
     * 6. Returns to Order Service
     * 
     * @GetMapping: HTTP GET method
     * @PathVariable: URL path variable (product ID)
     * 
     * @param id - Product का ID
     * @return ProductDto - Product information
     * 
     * ERROR HANDLING:
     * - If Product Service unavailable → Fallback called
     * - If timeout → Fallback called
     * - Circuit breaker opens if too many failures
     * 
     * EXAMPLE:
     * Order Service: productClient.getProduct(1L)
     * ↓
     * Feign: GET http://product-service/api/products/1
     * ↓
     * Product Service: Returns ProductDto
     * ↓
     * Order Service: Receives ProductDto
     */
    @GetMapping("/api/products/{id}") // GET endpoint in Product Service
    ProductDto getProduct(@PathVariable Long id); // Extract ID from URL path
}
