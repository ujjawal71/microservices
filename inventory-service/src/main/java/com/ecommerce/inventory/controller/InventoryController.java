package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * INVENTORY CONTROLLER - REST API Layer
 * 
 * PURPOSE (उद्देश्य):
 * - HTTP requests handle करना
 * - REST API endpoints provide करना
 * - Request validation और response formatting
 * 
 * CONTROLLER PATTERN:
 * - Frontend/Other services से HTTP requests receive करता है
 * - Service layer को delegate करता है (business logic नहीं करता)
 * - HTTP response format करता है
 * 
 * REST API CONVENTIONS:
 * - GET /api/inventory/product/{id} - Read data
 * - PUT /api/inventory/product/{id} - Update data
 * - POST /api/inventory/reserve - Create action
 * 
 * @RestController = @Controller + @ResponseBody
 * - सभी methods automatically JSON return करते हैं
 * - No need for @ResponseBody on each method
 * 
 * @RequestMapping("/api/inventory") - Base path for all endpoints
 * - All URLs start with /api/inventory
 * 
 * @CrossOrigin(origins = "*") - CORS (Cross-Origin Resource Sharing)
 * - Frontend (React app) से requests allow करता है
 * - Different port (3000) से requests accept करता है
 * - Production में specific origins specify करें (security)
 */
@RestController // REST API controller (returns JSON responses)
@RequestMapping("/api/inventory") // Base URL path
@CrossOrigin(origins = "*") // Allow requests from any origin (CORS)
public class InventoryController {
    
    /**
     * SERVICE DEPENDENCY
     * Controller business logic नहीं करता, Service को delegate करता है
     * Separation of Concerns: Controller = HTTP handling, Service = Business logic
     */
    private final InventoryService inventoryService;
    
    /**
     * CONSTRUCTOR INJECTION
     * Spring automatically InventoryService inject करता है
     */
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    /**
     * GET INVENTORY BY PRODUCT ID
     * 
     * HTTP Method: GET
     * URL: GET /api/inventory/product/{productId}
     * 
     * USE CASE:
     * - Frontend product detail page पर stock show करने के लिए
     * - "Available: 90 units" display करना
     * 
     * @GetMapping: GET request handle करता है
     * @PathVariable: URL से productId extract करता है
     * 
     * @param productId - URL में से product ID
     * @return ResponseEntity<Inventory> - HTTP 200 (OK) with inventory data, or 404 (Not Found)
     * 
     * RESPONSE CODES:
     * - 200 OK: Inventory found and returned
     * - 404 Not Found: Product inventory doesn't exist
     * 
     * EXAMPLE REQUEST:
     * GET http://localhost:8085/api/inventory/product/1
     * 
     * EXAMPLE RESPONSE (200 OK):
     * {
     *   "id": 1,
     *   "productId": 1,
     *   "quantity": 100,
     *   "reservedQuantity": 10,
     *   "availableQuantity": 90
     * }
     */
    @GetMapping("/product/{productId}") // GET /api/inventory/product/{productId}
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable Long productId) {
        // Get inventory from service layer
        Optional<Inventory> inventory = inventoryService.getInventoryByProductId(productId);
        
        // If found, return 200 OK with data
        // If not found, return 404 Not Found
        return inventory.map(ResponseEntity::ok) // If present, return 200 OK
                .orElse(ResponseEntity.notFound().build()); // If empty, return 404
    }
    
    /**
     * UPDATE INVENTORY
     * 
     * HTTP Method: PUT
     * URL: PUT /api/inventory/product/{productId}?quantity=150
     * 
     * USE CASE:
     * - Admin stock manually update करना चाहता है
     * - नया stock warehouse में आ गया है
     * - Stock correction करना है
     * 
     * @PutMapping: PUT request handle करता है (Update operation)
     * @PathVariable: URL से productId
     * @RequestParam: Query parameter से quantity
     * 
     * @param productId - URL में से product ID
     * @param quantity - Query parameter से नया quantity
     * @return ResponseEntity<Inventory> - Updated inventory data
     * 
     * EXAMPLE REQUEST:
     * PUT http://localhost:8085/api/inventory/product/1?quantity=150
     * 
     * EXAMPLE RESPONSE:
     * {
     *   "id": 1,
     *   "productId": 1,
     *   "quantity": 150,
     *   "reservedQuantity": 10,
     *   "availableQuantity": 140
     * }
     */
    @PutMapping("/product/{productId}") // PUT /api/inventory/product/{productId}
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long productId,
                                                     @RequestParam Integer quantity) {
        // Update inventory through service layer
        Inventory updatedInventory = inventoryService.updateInventory(productId, quantity);
        // Return 200 OK with updated data
        return ResponseEntity.ok(updatedInventory);
    }
    
    /**
     * RESERVE INVENTORY
     * 
     * HTTP Method: POST
     * URL: POST /api/inventory/reserve?productId=1&quantity=5
     * 
     * USE CASE:
     * - Order service order create करते समय stock reserve करने के लिए call करता है
     * - Payment pending होने पर stock hold करना
     * - Other services से programmatically call होता है (not directly from frontend)
     * 
     * @PostMapping: POST request handle करता है (Action/Create)
     * @RequestParam: Query parameters से data
     * 
     * @param productId - Product का ID
     * @param quantity - कितना stock reserve करना है
     * @return ResponseEntity<Boolean> - true if reserved, false if insufficient stock
     * 
     * RESPONSE CODES:
     * - 200 OK with true: Successfully reserved
     * - 200 OK with false: Insufficient stock
     * 
     * EXAMPLE REQUEST:
     * POST http://localhost:8085/api/inventory/reserve?productId=1&quantity=5
     * 
     * EXAMPLE RESPONSE (Success):
     * true
     * 
     * EXAMPLE RESPONSE (Insufficient Stock):
     * false
     */
    @PostMapping("/reserve") // POST /api/inventory/reserve
    public ResponseEntity<Boolean> reserveInventory(@RequestParam Long productId,
                                                   @RequestParam Integer quantity) {
        // Reserve inventory through service layer
        boolean reserved = inventoryService.reserveInventory(productId, quantity);
        // Return result (true = success, false = insufficient stock)
        return ResponseEntity.ok(reserved);
    }
}
