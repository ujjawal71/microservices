package com.ecommerce.product.service;

import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * PRODUCT SERVICE - Product Management Business Logic
 * 
 * PURPOSE (उद्देश्य):
 * - Product CRUD operations
 * - Product search और filtering
 * - Circuit Breaker pattern implementation
 * 
 * CIRCUIT BREAKER CONCEPT:
 * - Resilience4j library use करता है
 * - If database/Inventory Service fails → Fallback method called
 * - Prevents service from crashing
 * - Graceful degradation
 * 
 * WHY CIRCUIT BREAKER:
 * - Database connection might fail
 * - External service (Inventory) might be down
 * - Instead of crashing → Return empty list or default value
 * - Better user experience
 */
@Service
public class ProductService {
    
    /**
     * PRODUCT REPOSITORY
     * Database operations के लिए
     */
    private final ProductRepository productRepository;
    
    /**
     * CONSTRUCTOR INJECTION
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * GET ALL PRODUCTS - Fetch all products with Circuit Breaker
     * 
     * @CircuitBreaker:
     * - Method को protect करता है
     * - If database fails → fallback method call होता है
     * - Circuit opens after threshold failures
     * - Prevents repeated database calls if database is down
     * 
     * FALLBACK BEHAVIOR:
     * - Returns empty list instead of crashing
     * - User sees empty product list (better than error page)
     * - Service remains available
     * 
     * @return List<Product> - All products, or empty list if fallback
     */
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getAllProductsFallback")
    public List<Product> getAllProducts() {
        // Try to fetch from database
        return productRepository.findAll();
    }
    
    /**
     * GET ALL PRODUCTS FALLBACK - Circuit Breaker Fallback
     * 
     * CALLED WHEN:
     * - Database connection fails
     * - Database timeout
     * - Circuit breaker opens
     * 
     * PURPOSE:
     * - Graceful degradation
     * - Return empty list instead of error
     * - Service remains responsive
     * 
     * @param ex - Exception that caused fallback
     * @return List<Product> - Empty list
     */
    public List<Product> getAllProductsFallback(Exception ex) {
        // Fallback when database or inventory service is unavailable
        // Return empty list - better than crashing
        // Log the error for monitoring
        System.err.println("Circuit breaker activated - returning empty list: " + ex.getMessage());
        return Collections.emptyList();
    }
    
    /**
     * GET PRODUCT BY ID
     * 
     * @param id - Product ID
     * @return Optional<Product> - Product if found, empty if not
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * GET PRODUCTS BY CATEGORY
     * 
     * Category-wise products filter करता है
     * 
     * @param category - Product category (e.g., "Electronics", "Clothing")
     * @return List<Product> - Products in that category
     */
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    /**
     * SEARCH PRODUCTS
     * 
     * Product name में search करता है (case-insensitive)
     * 
     * @param query - Search keyword
     * @return List<Product> - Matching products
     */
    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }
    
    /**
     * CREATE PRODUCT
     * 
     * New product add करता है
     * 
     * @param product - Product object to create
     * @return Product - Created product (with generated ID)
     */
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    /**
     * UPDATE PRODUCT
     * 
     * Existing product update करता है
     * 
     * @param id - Product ID
     * @param product - Updated product data
     * @return Product - Updated product
     * @throws RuntimeException - If product not found
     */
    public Product updateProduct(Long id, Product product) {
        // Find existing product
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Update fields
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setCategory(product.getCategory());
        existing.setImageUrl(product.getImageUrl());
        existing.setStockQuantity(product.getStockQuantity());
        
        // Save updated product
        return productRepository.save(existing);
    }
    
    /**
     * DELETE PRODUCT
     * 
     * Product delete करता है
     * 
     * @param id - Product ID to delete
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
