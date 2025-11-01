package com.ecommerce.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ORDER CLIENT - Feign Client for Inter-Service Communication
 * 
 * PURPOSE (उद्देश्य):
 * - Payment Service से Order Service को call करना
 * - Payment success पर order status update करना
 * 
 * INTER-SERVICE COMMUNICATION:
 * Payment Service → Order Service
 * 
 * USE CASE:
 * - Payment verified और completed होने पर
 * - Order status को PENDING → CONFIRMED update करना
 * 
 * FEIGN CLIENT CONCEPT:
 * - Declarative HTTP client
 * - Spring automatically implementation generate करता है
 * - Service Discovery through Eureka
 * - Load balancing built-in
 */
@FeignClient(name = "order-service", url = "") // Service name in Eureka
public interface OrderClient {
    
    /**
     * UPDATE ORDER STATUS
     * 
     * Order Service में order status update करता है
     * 
     * CALLED WHEN:
     * - Payment verification successful हो जाता है
     * - Payment completed हो जाता है
     * 
     * STATUS CHANGE:
     * PENDING → CONFIRMED
     * 
     * @PutMapping: PUT HTTP method (update operation)
     * @PathVariable: URL path variable (order ID)
     * @RequestParam: Query parameter (status value)
     * 
     * @param id - Order ID to update
     * @param status - New status ("CONFIRMED", "SHIPPED", etc.)
     * @return Object - Response from Order Service
     * 
     * EXAMPLE:
     * Payment Service: orderClient.updateOrderStatus(18L, "CONFIRMED")
     * ↓
     * Feign: PUT http://order-service/api/orders/18/status?status=CONFIRMED
     * ↓
     * Order Service: Updates order status and returns response
     */
    @PutMapping("/api/orders/{id}/status")
    Object updateOrderStatus(@PathVariable Long id, @RequestParam String status);
}
