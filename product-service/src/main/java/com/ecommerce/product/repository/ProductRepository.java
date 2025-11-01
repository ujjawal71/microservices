package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PRODUCT REPOSITORY - Data Access Layer
 * 
 * ========================================================================
 * SPRING DATA JPA CONCEPTS
 * ========================================================================
 * 
 * METHOD NAMING CONVENTION:
 * - Spring Data JPA automatically generates queries from method names
 * - findByCategory() → "SELECT * FROM products WHERE category = ?"
 * - findByNameContainingIgnoreCase() → "SELECT * FROM products WHERE LOWER(name) LIKE LOWER(?)"
 * 
 * NO SQL NEEDED:
 * - Method names → Queries
 * - Type-safe (compile-time checking)
 * - Less boilerplate code
 * 
 * ========================================================================
 * QUERY GENERATION EXAMPLES
 * ========================================================================
 * 
 * findByCategory:
 * - Method: findByCategory(String category)
 * - Generated SQL: SELECT * FROM products WHERE category = ?
 * 
 * findByNameContainingIgnoreCase:
 * - Method: findByNameContainingIgnoreCase(String name)
 * - Generated SQL: SELECT * FROM products WHERE LOWER(name) LIKE LOWER(?)
 * - LIKE pattern: %name% (contains)
 * - Case-insensitive search
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * FIND BY CATEGORY
     * 
     * Spring Data JPA automatically generates:
     * SELECT * FROM products WHERE category = ?
     * 
     * @param category - Product category
     * @return List<Product> - Products in that category
     */
    List<Product> findByCategory(String category);
    
    /**
     * FIND BY NAME (CONTAINS, CASE-INSENSITIVE)
     * 
     * Spring Data JPA automatically generates:
     * SELECT * FROM products WHERE LOWER(name) LIKE LOWER(?)
     * Pattern: %name% (contains)
     * 
     * USE CASE:
     * - Product search functionality
     * - Case-insensitive search
     * 
     * @param name - Search keyword (part of product name)
     * @return List<Product> - Matching products
     */
    List<Product> findByNameContainingIgnoreCase(String name);
}
