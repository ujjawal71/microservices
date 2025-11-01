package com.ecommerce.product.controller;

import com.ecommerce.product.model.Product;
import com.ecommerce.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * PRODUCT CONTROLLER - REST API for Product Management
 * 
 * PURPOSE (उद्देश्य):
 * - Product CRUD operations expose करना
 * - Frontend को product data provide करना
 * - Pagination support
 * 
 * REST API ENDPOINTS:
 * - GET /api/products - Get all products (with pagination)
 * - GET /api/products/{id} - Get product by ID
 * - GET /api/products/category/{category} - Get products by category
 * - GET /api/products/search?q=keyword - Search products
 * - POST /api/products - Create product
 * - PUT /api/products/{id} - Update product
 * - DELETE /api/products/{id} - Delete product
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    /**
     * PRODUCT SERVICE DEPENDENCY
     */
    private final ProductService productService;
    
    /**
     * CONSTRUCTOR INJECTION
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * GET ALL PRODUCTS - With Pagination Support
     * 
     * FLOW:
     * 1. All products fetch करो
     * 2. Pagination apply करो (page, size parameters)
     * 3. X-Total-Count header में total count भेजो
     * 4. Paginated list return करो
     * 
     * PAGINATION CONCEPT:
     * - Large datasets को chunks में return करता है
     * - Performance improvement (less data transfer)
     * - Frontend infinite scroll के लिए useful
     * 
     * QUERY PARAMETERS:
     * - page: Page number (0-indexed, default: 0)
     * - size: Items per page (default: 100)
     * 
     * EXAMPLE:
     * GET /api/products?page=0&size=20
     * - Returns first 20 products
     * - Header: X-Total-Count: 100 (total products available)
     * 
     * @param page - Page number (0-based index)
     * @param size - Number of items per page
     * @return ResponseEntity<List<Product>> - Paginated products with total count header
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size) {
        // Fetch all products from service
        List<Product> allProducts = productService.getAllProducts();
        
        // Calculate pagination boundaries
        int start = page * size; // Start index
        int end = Math.min(start + size, allProducts.size()); // End index (don't exceed list size)
        
        // If page number too high → return empty list
        if (start >= allProducts.size()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        // Extract paginated sublist
        List<Product> paginatedProducts = allProducts.subList(start, end);
        
        // Return with X-Total-Count header (frontend pagination के लिए)
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(allProducts.size())) // Total count for frontend
                .body(paginatedProducts); // Paginated products
    }
    
    /**
     * GET PRODUCT BY ID
     * 
     * Single product details fetch करता है
     * 
     * @param id - Product ID
     * @return ResponseEntity<Product> - 200 OK with product, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok) // If found → 200 OK
                .orElse(ResponseEntity.notFound().build()); // If not found → 404
    }
    
    /**
     * GET PRODUCTS BY CATEGORY
     * 
     * Category-wise products filter करता है
     * 
     * @param category - Product category
     * @return ResponseEntity<List<Product>> - Products in category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }
    
    /**
     * SEARCH PRODUCTS
     * 
     * Product name में search करता है
     * 
     * @param q - Search query/keyword
     * @return ResponseEntity<List<Product>> - Matching products
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productService.searchProducts(q));
    }
    
    /**
     * CREATE PRODUCT
     * 
     * New product add करता है
     * 
     * @param product - Product object to create
     * @return ResponseEntity<Product> - 201 Created with created product
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(product));
    }
    
    /**
     * UPDATE PRODUCT
     * 
     * Existing product update करता है
     * 
     * @param id - Product ID
     * @param product - Updated product data
     * @return ResponseEntity<Product> - 200 OK with updated product, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            return ResponseEntity.ok(productService.updateProduct(id, product));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // Product not found
        }
    }
    
    /**
     * DELETE PRODUCT
     * 
     * Product delete करता है
     * 
     * @param id - Product ID to delete
     * @return ResponseEntity<Void> - 204 No Content (success)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
    
    /**
     * DECREMENT STOCK
     * 
     * Order confirmed होने पर product stock कम करने के लिए
     * 
     * USE CASE:
     * - Payment success → Order CONFIRMED
     * - Stock deduct करना (2 stock था, 1 order → 1 stock हो जाएगा)
     * 
     * @param id - Product ID
     * @param quantity - कितना stock कम करना है
     * @return ResponseEntity<Product> - Updated product
     */
    @PostMapping("/{id}/decrement-stock")
    public ResponseEntity<?> decrementStock(@PathVariable Long id,
                                           @RequestParam Integer quantity) {
        try {
            Product updatedProduct = productService.decrementStock(id, quantity);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * GET IN STOCK PRODUCTS
     * 
     * Get only products that are currently in stock (stockQuantity > 0)
     * 
     * USE CASE:
     * - Frontend can show only available products
     * - Hide out-of-stock products from catalog
     * - Better user experience
     * 
     * @return ResponseEntity<List<Product>> - Products that are in stock
     */
    @GetMapping("/in-stock")
    public ResponseEntity<List<Product>> getInStockProducts(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size) {
        List<Product> inStockProducts = productService.getInStockProducts();
        
        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, inStockProducts.size());
        
        if (start >= inStockProducts.size()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        List<Product> paginatedProducts = inStockProducts.subList(start, end);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(inStockProducts.size()))
                .body(paginatedProducts);
    }
    
    /**
     * GET OUT OF STOCK PRODUCTS
     * 
     * Get only products that are currently out of stock (stockQuantity <= 0)
     * 
     * USE CASE:
     * - Admin can see which products need restocking
     * - Inventory management
     * - Restock notifications
     * 
     * @return ResponseEntity<List<Product>> - Products that are out of stock
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStockProducts(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size) {
        List<Product> outOfStockProducts = productService.getOutOfStockProducts();
        
        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, outOfStockProducts.size());
        
        if (start >= outOfStockProducts.size()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        List<Product> paginatedProducts = outOfStockProducts.subList(start, end);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(outOfStockProducts.size()))
                .body(paginatedProducts);
    }
}
