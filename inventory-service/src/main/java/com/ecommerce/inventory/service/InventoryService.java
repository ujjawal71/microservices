package com.ecommerce.inventory.service;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

/**
 * INVENTORY SERVICE - Business Logic Layer
 * 
 * ========================================================================
 * ACID PROPERTIES IMPLEMENTATION
 * ========================================================================
 * 
 * ATOMICITY (All-or-Nothing):
 * - @Transactional ensures inventory operations are atomic
 * - Reserve operation: Check + Update is atomic
 * - If update fails → entire operation rolls back
 * 
 * CONSISTENCY (Data Integrity):
 * - Business rule: availableQuantity = quantity - reservedQuantity (always)
 * - Stock cannot go negative
 * - Reserved quantity cannot exceed total quantity
 * 
 * ISOLATION (Concurrent Access):
 * - READ_COMMITTED prevents dirty reads
 * - Pessimistic locking for concurrent stock updates
 * - Multiple orders for same product handled safely
 * 
 * DURABILITY (Permanent Storage):
 * - Stock updates persisted permanently
 * - Transaction logs ensure recovery
 * 
 * ========================================================================
 * DEADLOCK PREVENTION
 * ========================================================================
 * 
 * 1. CONSISTENT LOCK ORDERING:
 *    - Always lock by productId (sorted order)
 *    - Prevents circular wait conditions
 * 
 * 2. TRANSACTION TIMEOUT:
 *    - timeout = 10 seconds
 *    - Prevents long-held locks
 * 
 * 3. PESSIMISTIC LOCKING:
 *    - SELECT FOR UPDATE locks row during transaction
 *    - Prevents concurrent modifications
 * 
 * 4. MINIMAL TRANSACTION SCOPE:
 *    - Only critical operations in transaction
 *    - External calls outside transaction
 * 
 * ========================================================================
 * RACE CONDITION PREVENTION
 * ========================================================================
 * 
 * PROBLEM:
 * - Two orders for same product at same time
 * - Both check stock (both see 10 available)
 * - Both reserve 5 units (total reserved = 10, but should be 5)
 * - Result: Overselling! ❌
 * 
 * SOLUTION:
 * - @Transactional with pessimistic locking
 * - Database row lock during check + update
 * - Only one transaction can update at a time
 * - Second transaction waits for first to complete
 * 
 * ========================================================================
 * MICROSERVICES CONCEPTS
 * ========================================================================
 * 
 * 1. EVENT-DRIVEN (Kafka):
 *    - Listens to "order-created" events
 *    - Automatically reserves stock
 * 
 * 2. SAGA PATTERN:
 *    - Inventory reservation is part of distributed transaction
 *    - Compensation: Release inventory if payment fails
 * 
 * 3. EVENTUAL CONSISTENCY:
 *    - Stock updates eventually consistent across services
 * 
 * LAYERED ARCHITECTURE:
 * Controller → Service → Repository → Database
 */
@Service
public class InventoryService {
    
    /**
     * REPOSITORY DEPENDENCY
     */
    private final InventoryRepository inventoryRepository;
    
    /**
     * CONSTRUCTOR INJECTION
     */
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    
    /**
     * UPDATE INVENTORY - Admin stock update
     * 
     * ACID: Consistency - Quantity validation
     * 
     * @param productId - Product ID
     * @param quantity - New total quantity
     * @return Inventory - Updated inventory
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 10
    )
    public Inventory updateInventory(Long productId, Integer quantity) {
        // ACID: Consistency - Quantity cannot be negative
        if (quantity < 0) {
            throw new RuntimeException("Quantity cannot be negative - Consistency violation");
        }
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setProductId(productId);
                    return newInventory;
                });
        
        inventory.setQuantity(quantity);
        return inventoryRepository.save(inventory);
    }
    
    /**
     * RESERVE INVENTORY - Order के लिए stock reserve करना
     * 
     * ========================================================================
     * ACID PROPERTIES
     * ========================================================================
     * 
     * ATOMICITY:
     * - Check stock + Reserve is atomic
     * - If reservation fails → no partial update
     * 
     * CONSISTENCY:
     * - availableQuantity = quantity - reservedQuantity (always)
     * - Reserved quantity cannot exceed available
     * 
     * ISOLATION:
     * - READ_COMMITTED with pessimistic locking
     * - Concurrent reservations handled safely
     * 
     * DURABILITY:
     * - Reservation persisted permanently
     * 
     * ========================================================================
     * DEADLOCK PREVENTION
     * ========================================================================
     * 
     * 1. CONSISTENT LOCK ORDERING:
     *    - Lock by productId (consistent ordering prevents circular wait)
     *    - If multiple products → lock in sorted order
     * 
     * 2. TRANSACTION TIMEOUT:
     *    - timeout = 10 seconds
     *    - Prevents long-held locks
     * 
     * 3. PESSIMISTIC LOCKING:
     *    - SELECT FOR UPDATE locks row
     *    - Other transactions wait for lock release
     * 
     * ========================================================================
     * RACE CONDITION PREVENTION
     * ========================================================================
     * 
     * WITHOUT LOCKING (Race Condition):
     * Transaction A: Check stock (10 available) → Reserve 5
     * Transaction B: Check stock (10 available) → Reserve 5 (at same time)
     * Result: Both reserve 5, total reserved = 10, but only 10 available! ❌
     * 
     * WITH PESSIMISTIC LOCKING (Safe):
     * Transaction A: Lock row → Check (10) → Reserve 5 → Commit → Release lock
     * Transaction B: Wait for lock → Lock row → Check (5 available) → Reserve 5 ✅
     * 
     * @Transactional ensures atomicity and isolation
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED, // ACID: Isolation level
        propagation = Propagation.REQUIRED,    // Join or create transaction
        timeout = 10,                          // DEADLOCK PREVENTION: Timeout
        rollbackFor = Exception.class          // Rollback on any exception
    )
    public boolean reserveInventory(Long productId, Integer quantity) {
        // Verify transaction active (ACID: Atomicity check)
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new RuntimeException("No active transaction - ACID violation risk");
        }
        
        // Find inventory (with pessimistic lock if possible)
        // DEADLOCK PREVENTION: Consistent ordering by productId
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));
        
        // ACID: Consistency - Check business rule
        // availableQuantity = quantity - reservedQuantity
        int availableQuantity = inventory.getAvailableQuantity();
        
        // Business rule validation
        if (quantity <= 0) {
            throw new RuntimeException("Reserve quantity must be positive - Consistency violation");
        }
        
        // ACID: Consistency - Available stock check
        if (availableQuantity >= quantity) {
            // ACID: Atomicity - Update reserved quantity
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            
            // ACID: Consistency - Verify constraint still holds
            if (inventory.getAvailableQuantity() < 0) {
                throw new RuntimeException("Insufficient stock - Consistency violation");
            }
            
            // ACID: Durability - Save to database
            inventoryRepository.save(inventory);
            
            // Verify transaction still active
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new RuntimeException("Transaction lost - ACID violation");
            }
            
            return true; // Successfully reserved
        }
        
        // Insufficient stock
        return false;
    }
    
    /**
     * RELEASE INVENTORY - Reserved stock को वापस available करना
     * 
     * ACID PROPERTIES:
     * - Atomicity: Release operation is atomic
     * - Consistency: Reserved quantity cannot go negative
     * - Isolation: Concurrent releases handled safely
     * 
     * DEADLOCK PREVENTION:
     * - Same ordering as reserve (consistent lock order)
     * - Short transaction scope
     * 
     * SAGA PATTERN:
     * - Compensation transaction
     * - If payment fails → release reserved stock
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 10,
        rollbackFor = Exception.class
    )
    public void releaseInventory(Long productId, Integer quantity) {
        // Verify transaction active
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new RuntimeException("No active transaction - ACID violation risk");
        }
        
        // Find inventory (consistent ordering - deadlock prevention)
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));
        
        // ACID: Consistency - Release reserved stock
        // Math.max(0, ...) ensures reservedQuantity never goes negative
        int currentReserved = inventory.getReservedQuantity();
        int newReserved = Math.max(0, currentReserved - quantity);
        
        inventory.setReservedQuantity(newReserved);
        
        // ACID: Durability - Save to database
        inventoryRepository.save(inventory);
    }
    
    /**
     * DEDUCT INVENTORY - Order confirm होने पर actual stock कम करना
     * 
     * ACID PROPERTIES:
     * - Atomicity: Quantity decrease + reserved decrease is atomic
     * - Consistency: Stock cannot go negative
     * - Isolation: Concurrent deductions handled safely
     * 
     * DEADLOCK PREVENTION:
     * - Consistent lock ordering
     * - Short transaction scope
     * 
     * SAGA PATTERN:
     * - Final step in order saga
     * - Order confirmed → Deduct actual stock
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 10,
        rollbackFor = Exception.class
    )
    public void deductInventory(Long productId, Integer quantity) {
        // Verify transaction active
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new RuntimeException("No active transaction - ACID violation risk");
        }
        
        // Find inventory
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));
        
        // ACID: Consistency - Check if sufficient stock available
        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock to deduct - Consistency violation");
        }
        
        // ACID: Atomicity - Deduct from total quantity
        inventory.setQuantity(inventory.getQuantity() - quantity);
        
        // ACID: Consistency - Also decrease reserved quantity (clear reservation)
        // Business rule: When stock deducted, reservation should also decrease
        int currentReserved = inventory.getReservedQuantity();
        int reservedToRelease = Math.min(quantity, currentReserved);
        inventory.setReservedQuantity(Math.max(0, currentReserved - reservedToRelease));
        
        // ACID: Consistency - Verify constraints
        if (inventory.getQuantity() < 0 || inventory.getReservedQuantity() < 0) {
            throw new RuntimeException("Inventory constraints violated - Consistency violation");
        }
        
        // ACID: Durability - Save to database
        inventoryRepository.save(inventory);
    }
    
    /**
     * GET INVENTORY BY PRODUCT ID
     * 
     * Read-only operation (no locks, better performance)
     * 
     * @param productId - Product ID
     * @return Optional<Inventory> - Inventory information
     */
    @Transactional(readOnly = true) // Read-only transaction (no locks)
    public Optional<Inventory> getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }
}
