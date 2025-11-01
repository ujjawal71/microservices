package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * INVENTORY CLIENT - Feign Client for Inventory Service Communication
 * 
 * PURPOSE (उद्देश्य):
 * - Order Service से Inventory Service को call करना
 * - Stock reserve करना order create करने से पहले
 * - Race condition handle करने के लिए atomic reservation
 * 
 * FEIGN CLIENT CONCEPT:
 * - Declarative HTTP client
 * - Spring automatically implementation generate करता है
 * - Service discovery through Eureka
 * 
 * RACE CONDITION HANDLING:
 * - Inventory Service uses pessimistic locking
 * - Only one order can reserve stock at a time
 * - Atomic operation prevents overselling
 * 
 * EXAMPLE SCENARIO:
 * - 1 stock left
 * - Customer A orders at same time as Customer B
 * - Inventory Service: Lock → Check (1 available) → Reserve for A → Commit
 * - Inventory Service: Lock → Check (0 available) → Return false to B
 * - Result: Only Customer A's order succeeds ✅
 */
@FeignClient(
    name = "inventory-service", // Service name in Eureka
    fallback = InventoryClientFallback.class,
    url = "" // Use service discovery
)
public interface InventoryClient {
    
    /**
     * RESERVE INVENTORY
     * 
     * Order Service order create करने से पहले stock reserve करने के लिए call करता है
     * 
     * RACE CONDITION PREVENTION:
     * - Inventory Service uses pessimistic locking
     * - Atomic check-and-reserve operation
     * - Only one transaction can reserve at a time
     * 
     * @param productId - Product ID
     * @param quantity - Quantity to reserve
     * @return Boolean - true if reserved successfully, false if insufficient stock
     * 
     * EXAMPLE:
     * Order Service: inventoryClient.reserveInventory(1L, 2)
     * ↓
     * Feign: POST http://inventory-service/api/inventory/reserve?productId=1&quantity=2
     * ↓
     * Inventory Service: Lock row → Check stock → Reserve → Return true/false
     * ↓
     * Order Service: Receives true/false
     */
    @PostMapping("/api/inventory/reserve") // POST endpoint in Inventory Service
    Boolean reserveInventory(@RequestParam("productId") Long productId,
                           @RequestParam("quantity") Integer quantity);
    
    /**
     * DEDUCT INVENTORY
     * 
     * Order confirmed होने पर actual stock कम करने के लिए
     * 
     * FLOW:
     * 1. Order created → Stock reserved
     * 2. Payment success → Order CONFIRMED
     * 3. This method called → Actual stock deducted
     * 
     * @param productId - Product ID
     * @param quantity - Quantity to deduct
     * @return Object - Response from Inventory Service
     */
    @PostMapping("/api/inventory/deduct") // POST endpoint in Inventory Service
    Object deductInventory(@RequestParam("productId") Long productId,
                          @RequestParam("quantity") Integer quantity);
}

