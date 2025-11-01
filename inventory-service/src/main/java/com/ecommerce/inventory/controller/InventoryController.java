package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable Long productId) {
        Optional<Inventory> inventory = inventoryService.getInventoryByProductId(productId);
        return inventory.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/product/{productId}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long productId,
                                                     @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.updateInventory(productId, quantity));
    }
    
    @PostMapping("/reserve")
    public ResponseEntity<Boolean> reserveInventory(@RequestParam Long productId,
                                                   @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.reserveInventory(productId, quantity));
    }
}

