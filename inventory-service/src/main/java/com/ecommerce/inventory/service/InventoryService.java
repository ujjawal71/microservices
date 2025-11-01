package com.ecommerce.inventory.service;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * INVENTORY SERVICE - Business Logic Layer
 * 
 * PURPOSE (उद्देश्य):
 * - Inventory management की business logic implement करना
 * - Controller और Repository के बीच middle layer
 * - Business rules enforce करना
 * 
 * LAYERED ARCHITECTURE:
 * Controller → Service → Repository → Database
 * 
 * WHY SERVICE LAYER:
 * - Business logic को controller से separate करता है
 * - Reusable code (multiple controllers use कर सकते हैं)
 * - Easy testing (mock repository)
 * - Single Responsibility Principle
 * 
 * DEPENDENCY INJECTION:
 * - Constructor injection use करते हैं (Spring best practice)
 * - Final field = immutable (thread-safe)
 * - Spring automatically dependency inject करता है
 */
@Service // Spring annotation: यह एक service component है (business logic)
public class InventoryService {
    
    /**
     * REPOSITORY DEPENDENCY
     * Final = Immutable (cannot change after construction)
     * Private = Encapsulation (only this class can access)
     * 
     * DEPENDENCY INJECTION:
     * Spring framework automatically InventoryRepository inject करता है
     * Constructor के through (no need to create manually)
     */
    private final InventoryRepository inventoryRepository;
    
    /**
     * CONSTRUCTOR INJECTION
     * 
     * Spring automatically यहाँ InventoryRepository pass करता है
     * @param inventoryRepository - Database access के लिए repository
     */
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    
    /**
     * UPDATE INVENTORY - Admin के लिए stock update करने के लिए
     * 
     * FLOW:
     * 1. Product ID से inventory ढूंढो
     * 2. अगर नहीं मिला → नया inventory create करो
     * 3. Quantity update करो
     * 4. Database में save करो
     * 
     * USE CASE:
     * - Admin नया stock add करना चाहता है
     * - Stock quantity manually update करना है
     * 
     * @param productId - Product का ID
     * @param quantity - नया total quantity
     * @return Inventory - Updated inventory object
     * 
     * DESIGN PATTERN: orElseGet() - Lazy initialization
     * - अगर inventory नहीं मिला, तभी नया object create करता है
     * - Memory efficient (unnecessary objects नहीं बनते)
     */
    public Inventory updateInventory(Long productId, Integer quantity) {
        // Try to find existing inventory
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    // If not found, create new inventory record
                    Inventory newInventory = new Inventory();
                    newInventory.setProductId(productId);
                    return newInventory;
                });
        // Update quantity
        inventory.setQuantity(quantity);
        // Save to database (insert if new, update if exists)
        return inventoryRepository.save(inventory);
    }
    
    /**
     * RESERVE INVENTORY - Order के लिए stock reserve करना
     * 
     * FLOW:
     * 1. Product ID से inventory ढूंढो
     * 2. Check करो: क्या sufficient stock available है?
     * 3. अगर है → reservedQuantity बढ़ाओ
     * 4. Database में save करो
     * 
     * USE CASE:
     * - Customer order देता है (payment pending)
     * - Stock को temporary reserve करना है
     * - दूसरे customers को यह stock दिखना चाहिए नहीं
     * 
     * @Transactional:
     * - यह method एक transaction में run होती है
     * - अगर error आए → automatic rollback (changes cancel)
     * - Data consistency guarantee करता है
     * 
     * @param productId - Product का ID जिसका stock reserve करना है
     * @param quantity - कितना stock reserve करना है
     * @return boolean - true if reserved successfully, false if insufficient stock
     * 
     * EXAMPLE:
     * - Available stock: 90 units
     * - Request: Reserve 5 units
     * - Result: reservedQuantity += 5, availableQuantity = 85 ✅
     */
    @Transactional // Atomic operation (all-or-nothing)
    public boolean reserveInventory(Long productId, Integer quantity) {
        // Find inventory (throw exception if not found)
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));
        
        // Check if sufficient stock available
        if (inventory.getAvailableQuantity() >= quantity) {
            // Reserve the stock (increase reserved quantity)
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);
            return true; // Successfully reserved
        }
        return false; // Insufficient stock
    }
    
    /**
     * RELEASE INVENTORY - Reserved stock को वापस available करना
     * 
     * FLOW:
     * 1. Product ID से inventory ढूंढो
     * 2. Reserved quantity कम करो
     * 3. Database में save करो
     * 
     * USE CASE:
     * - Payment fail हो गया
     * - Order cancel हो गया
     * - Stock वापस available करना है
     * 
     * @Transactional: Data consistency के लिए
     * 
     * @param productId - Product का ID
     * @param quantity - कितना stock release करना है
     * 
     * Math.max(0, ...) - Negative values prevent करता है
     * - reservedQuantity कभी negative नहीं हो सकता
     * 
     * EXAMPLE:
     * - Reserved: 10 units
     * - Release: 5 units
     * - Result: reservedQuantity = 5, availableQuantity बढ़ जाएगा ✅
     */
    @Transactional
    public void releaseInventory(Long productId, Integer quantity) {
        // Find inventory
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));
        
        // Release reserved stock (decrease reserved quantity)
        // Math.max(0, ...) ensures reservedQuantity never goes negative
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventoryRepository.save(inventory);
    }
    
    /**
     * DEDUCT INVENTORY - Order confirm होने पर actual stock कम करना
     * 
     * FLOW:
     * 1. Product ID से inventory ढूंढो
     * 2. Total quantity कम करो (actual stock decrease)
     * 3. Reserved quantity भी कम करो (reservation clear)
     * 4. Database में save करो
     * 
     * USE CASE:
     * - Payment successful हो गया
     * - Order confirmed हो गया
     * - Actual stock warehouse से निकल गया
     * 
     * @Transactional: Critical operation - must be atomic
     * 
     * @param productId - Product का ID
     * @param quantity - कितना stock deduct करना है
     * 
     * EXAMPLE:
     * - Total quantity: 100
     * - Reserved: 10
     * - Deduct: 5 units (order confirmed)
     * - Result: quantity = 95, reservedQuantity = 5 ✅
     */
    @Transactional
    public void deductInventory(Long productId, Integer quantity) {
        // Find inventory
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));
        
        // Deduct from total quantity (actual stock decrease)
        inventory.setQuantity(inventory.getQuantity() - quantity);
        // Also decrease reserved quantity (clear reservation)
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventoryRepository.save(inventory);
    }
    
    /**
     * GET INVENTORY BY PRODUCT ID - Inventory information fetch करना
     * 
     * USE CASE:
     * - Frontend को stock information show करना
     * - Product detail page पर "In Stock: 90 units" display करना
     * - Admin dashboard पर stock check करना
     * 
     * @param productId - Product का ID
     * @return Optional<Inventory> - Inventory information (यदि exists)
     * 
     * WHY OPTIONAL:
     * - New product might not have inventory record yet
     * - Safe null handling
     * - Caller decides how to handle missing inventory
     */
    public Optional<Inventory> getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }
}

