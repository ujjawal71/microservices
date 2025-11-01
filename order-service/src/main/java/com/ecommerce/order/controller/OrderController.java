package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ORDER CONTROLLER - Order Management REST API
 * 
 * PURPOSE (उद्देश्य):
 * - Order creation और management endpoints
 * - Order retrieval (by user, by ID, all orders)
 * - Order status updates
 * 
 * REST API ENDPOINTS:
 * - POST /api/orders - Create new order
 * - GET /api/orders/all - Get all orders (admin)
 * - GET /api/orders/user/{userId} - Get orders by user
 * - GET /api/orders/{id} - Get order by ID
 * - PUT /api/orders/{id}/status - Update order status
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    /**
     * ORDER SERVICE DEPENDENCY
     */
    private final OrderService orderService;
    
    /**
     * CONSTRUCTOR INJECTION
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * CREATE ORDER
     * 
     * New order create करता है
     * 
     * FLOW (OrderService में):
     * 1. Order object create करो
     * 2. Product Service से product details fetch करो (Feign Client)
     * 3. Order items create करो
     * 4. Total amount calculate करो
     * 5. Order save करो (database)
     * 6. Kafka event publish करो (async) - Inventory/Notification services notify करने के लिए
     * 
     * @param request - Order request (userId, items[], shippingAddress)
     * @return ResponseEntity - Created order (201 Created) or error (500)
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            System.out.println("OrderController: Received order creation request for userId: " + request.getUserId());
            
            // Delegate to service layer
            Order order = orderService.createOrder(request);
            
            System.out.println("OrderController: Order created successfully with ID: " + order.getId());
            
            // Return 201 Created with order object
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception e) {
            // Error handling
            System.err.println("OrderController: Error creating order: " + e.getMessage());
            e.printStackTrace();
            
            // Return 500 Internal Server Error with error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order: " + e.getMessage());
        }
    }
    
    /**
     * GET ALL ORDERS
     * 
     * Admin के लिए सभी orders fetch करता है
     * 
     * USE CASE:
     * - Admin panel में सभी orders display करना
     * - Order management dashboard
     * 
     * ROUTE ORDER IMPORTANT:
     * /all must come before /{id}
     * Otherwise Spring will match /all as /{id} (treating "all" as ID)
     * 
     * @return ResponseEntity<List<Order>> - All orders
     */
    @GetMapping("/all") // Must come before /{id} to avoid route conflicts
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    /**
     * GET ORDERS BY USER ID
     * 
     * User के सभी orders fetch करता है
     * 
     * USE CASE:
     * - User order history page
     * - "My Orders" section
     * 
     * @param userId - User ID
     * @return ResponseEntity<List<Order>> - User's orders
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }
    
    /**
     * GET ORDER BY ID
     * 
     * Specific order details fetch करता है
     * 
     * @param id - Order ID
     * @return ResponseEntity<Order> - Order details (200 OK) or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
    
    /**
     * UPDATE ORDER STATUS
     * 
     * Order status update करता है
     * 
     * STATUS FLOW:
     * PENDING → CONFIRMED → SHIPPED → DELIVERED
     * 
     * USE CASE:
     * - Admin order status change करता है
     * - Payment Service payment success पर status CONFIRMED करता है
     * 
     * @param id - Order ID
     * @param status - New status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
     * @return ResponseEntity<Order> - Updated order (200 OK) or 404 Not Found
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, 
                                                   @RequestParam Order.OrderStatus status) {
        try {
            return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}
