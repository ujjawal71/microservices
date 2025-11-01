package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.ProductDto;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ORDER SERVICE - Order Management Business Logic
 * 
 * PURPOSE (उद्देश्य):
 * - Order creation और management
 * - Product Service से communication (Feign Client)
 * - Event publishing to Kafka (Event-Driven Architecture)
 * - Circuit Breaker pattern implementation
 * 
 * KEY CONCEPTS:
 * 1. INTER-SERVICE COMMUNICATION (Feign Client)
 *    - Order Service → Product Service (fetch product details)
 *    - Service Discovery through Eureka
 * 
 * 2. CIRCUIT BREAKER PATTERN
 *    - If Product Service fails → Fallback method called
 *    - Prevents cascading failures
 *    - Resilience4j library
 * 
 * 3. EVENT-DRIVEN ARCHITECTURE (Kafka)
 *    - Order created → Kafka event published
 *    - Other services listen and react (Inventory, Notification, Payment)
 *    - Asynchronous communication
 * 
 * 4. TRANSACTION MANAGEMENT
 *    - @Transactional ensures atomicity
 *    - All-or-nothing (rollback on error)
 */
@Service
public class OrderService {
    
    /**
     * ORDER REPOSITORY
     * Database operations के लिए
     */
    private final OrderRepository orderRepository;
    
    /**
     * PRODUCT CLIENT (Feign Client)
     * Product Service को call करने के लिए
     * Service Discovery through Eureka
     */
    private final ProductClient productClient;
    
    /**
     * KAFKA TEMPLATE
     * Kafka topics पर events publish करने के लिए
     * Asynchronous messaging के लिए
     */
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * CONSTRUCTOR INJECTION
     * Spring automatically dependencies inject करता है
     */
    public OrderService(OrderRepository orderRepository, ProductClient productClient,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * CREATE ORDER - Main order creation method
     * 
     * FLOW:
     * 1. Order object create करो (userId, address, status)
     * 2. Each product के लिए Product Service को call करो (Feign Client)
     * 3. Product details fetch करो (name, price)
     * 4. Order items create करो
     * 5. Total amount calculate करो
     * 6. Order save करो (database)
     * 7. Kafka event publish करो (async) - Other services notify करने के लिए
     * 
     * @Transactional:
     * - यह method एक transaction में run होती है
     * - अगर किसी step में error आए → सभी changes rollback
     * - Data consistency guarantee
     * 
     * @CircuitBreaker:
     * - Resilience4j circuit breaker
     * - If Product Service fails repeatedly → Circuit opens
     * - Fallback method (createOrderFallback) call होता है
     * - Prevents cascading failures
     * 
     * INTER-SERVICE COMMUNICATION:
     * Order Service → Feign Client → Product Service (Eureka Discovery)
     * 
     * EVENT-DRIVEN:
     * Order created → Kafka event → Inventory/Notification/Payment services listen
     * 
     * @param request - Order request (userId, items, shipping address)
     * @return Order - Created order object
     * 
     * EXAMPLE:
     * Request: {userId: 1, items: [{productId: 1, quantity: 2}], address: "Delhi"}
     * Flow:
     * 1. Call Product Service → Get product details (iPhone, ₹50000)
     * 2. Create OrderItem (iPhone x 2 = ₹100000)
     * 3. Save Order to database
     * 4. Publish Kafka event "order-created"
     */
    @Transactional // Atomic operation (all-or-nothing)
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    public Order createOrder(OrderRequest request) {
        // Step 1: Create Order object
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(Order.OrderStatus.PENDING); // Initial status
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Step 2: Process each order item
        List<OrderItem> items = new ArrayList<>();
        for (var itemDto : request.getItems()) {
            try {
                System.out.println("Fetching product: " + itemDto.getProductId());
                
                // INTER-SERVICE CALL (Feign Client)
                // This calls Product Service through Eureka Service Discovery
                // If Product Service is down → Circuit Breaker handles it
                ProductDto product = productClient.getProduct(itemDto.getProductId());
                System.out.println("Product fetched: " + product.getId() + " - " + product.getName());
                
                // Step 3: Create OrderItem from Product details
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order); // Link to parent order
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName()); // Store name (denormalization for performance)
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(product.getPrice()); // Store price snapshot (price may change later)
                
                items.add(orderItem);
            } catch (Exception e) {
                // If Product Service call fails
                System.err.println("Failed to fetch product " + itemDto.getProductId() + ": " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to fetch product information: " + e.getMessage(), e);
            }
        }
        
        // Step 4: Calculate total amount
        // Using Stream API: sum of (price * quantity) for all items
        totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))) // price * quantity
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum all
        
        // Step 5: Set items and total amount to order
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        
        // Step 6: Save order to database
        Order savedOrder = orderRepository.save(order);
        
        // Step 7: Publish Kafka event (ASYNCHRONOUS - Non-blocking)
        // Run in separate thread to ensure it never blocks order creation
        // Kafka is optional - if it fails, order creation still succeeds
        new Thread(() -> {
            try {
                // Publish to "order-created" topic
                // Other services (Inventory, Notification, Payment) listen to this topic
                kafkaTemplate.send("order-created", savedOrder);
                System.out.println("Kafka event published (async)");
            } catch (Exception e) {
                // Log but don't fail - Kafka is optional
                // Order creation should succeed even if Kafka is down
                System.err.println("Kafka publish failed (non-critical): " + e.getMessage());
            }
        }).start();
        
        return savedOrder;
    }
    
    /**
     * CREATE ORDER FALLBACK - Circuit Breaker Fallback Method
     * 
     * CALLED WHEN:
     * - Product Service is unavailable
     * - Circuit breaker opens (too many failures)
     * - Network timeout
     * 
     * PURPOSE:
     * - Graceful degradation
     * - Return error message instead of crashing
     * - Prevent cascading failures
     * 
     * @param request - Original order request
     * @param ex - Exception that caused fallback
     * @return Order - Never returns (throws exception)
     * @throws RuntimeException - Error message for user
     */
    public Order createOrderFallback(OrderRequest request, Exception ex) {
        // Fallback when order creation fails
        // Return user-friendly error message
        throw new RuntimeException("Order service temporarily unavailable. Please try again later.", ex);
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
     * @param userId - User का ID
     * @return List<Order> - User के सभी orders
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * GET ALL ORDERS
     * 
     * Admin के लिए सभी orders fetch करता है
     * 
     * USE CASE:
     * - Admin dashboard
     * - Order management panel
     * 
     * @return List<Order> - सभी orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    /**
     * GET ORDER BY ID
     * 
     * Specific order details fetch करता है
     * 
     * @param id - Order ID
     * @return Order - Order object
     * @throws RuntimeException - If order not found
     */
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    /**
     * UPDATE ORDER STATUS
     * 
     * Order status update करता है (PENDING → CONFIRMED → SHIPPED → DELIVERED)
     * 
     * USE CASE:
     * - Admin order status change करता है
     * - Payment success पर status CONFIRMED हो जाता है
     * 
     * @param id - Order ID
     * @param status - New status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
     * @return Order - Updated order
     */
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = getOrderById(id); // Fetch order
        order.setStatus(status); // Update status
        return orderRepository.save(order); // Save to database
    }
}
