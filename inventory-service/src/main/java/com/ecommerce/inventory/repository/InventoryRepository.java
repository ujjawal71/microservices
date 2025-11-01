package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * INVENTORY REPOSITORY - Data Access Layer
 * 
 * PURPOSE (उद्देश्य):
 * - Database operations (CRUD) के लिए interface
 * - Custom queries define करने के लिए
 * 
 * SPRING DATA JPA CONCEPT:
 * - JpaRepository extend करने से automatically methods मिलते हैं:
 *   - save() - Insert/Update
 *   - findById() - Find by primary key
 *   - findAll() - Get all records
 *   - delete() - Delete record
 * 
 * METHOD NAMING CONVENTION:
 * - findByProductId() = "SELECT * FROM inventory WHERE product_id = ?"
 * - Spring automatically query generate करता है method name से!
 * - No SQL code needed! ✅
 * 
 * WHY OPTIONAL<T>:
 * - Product might not exist in inventory
 * - Optional handles null cases safely
 * - Prevents NullPointerException
 * 
 * DESIGN PATTERN: Repository Pattern
 * - Service layer को database details से isolate करता है
 * - Easy testing (mock repository)
 * - Single Responsibility Principle
 */
@Repository // Spring annotation: यह एक repository component है
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    /**
     * FIND BY PRODUCT ID
     * 
     * यह method Spring Data JPA automatically implement करता है
     * Method name से query generate होता है:
     * 
     * SQL Equivalent:
     * SELECT * FROM inventory WHERE product_id = ?
     * 
     * @param productId - Product का ID जिसका inventory चाहिए
     * @return Optional<Inventory> - Product का inventory (यदि exists)
     * 
     * WHY OPTIONAL:
     * - Product inventory में नहीं हो सकता (new product)
     * - Optional.empty() return करता है अगर नहीं मिला
     * - Null safety के लिए better than returning null
     */
    Optional<Inventory> findByProductId(Long productId);
}

