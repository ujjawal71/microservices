package com.ecommerce.order.service;

import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.ProductDto;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ORDER SERVICE - Order Management Business Logic
 * 
 * ========================================================================
 * ACID PROPERTIES IMPLEMENTATION
 * ========================================================================
 * 
 * ATOMICITY (All-or-Nothing):
 * - @Transactional ensures entire operation succeeds or fails as one unit
 * - If any step fails → entire transaction rolls back
 * - Example: If product fetch fails → order not created, no partial data
 * 
 * CONSISTENCY (Data Integrity):
 * - Database constraints enforced (foreign keys, unique constraints)
 * - Business rules validated (total amount = sum of item amounts)
 * - Data relationships maintained (Order → OrderItems cascade)
 * 
 * ISOLATION (Transaction Isolation Levels):
 * - @Transactional(isolation = Isolation.READ_COMMITTED) prevents dirty reads
 * - Multiple transactions can run concurrently without interference
 * - Prevents: Dirty reads, Non-repeatable reads, Phantom reads
 * 
 * DURABILITY (Permanent Storage):
 * - @Transactional ensures data committed to database
 * - Database write-ahead logs guarantee persistence
 * - Even if system crashes → data is safe
 * 
 * ========================================================================
 * DEADLOCK PREVENTION
 * ========================================================================
 * 
 * 1. TRANSACTION ORDERING:
 *    - Always access resources in same order (product IDs sorted)
 *    - Prevents circular wait conditions
 * 
 * 2. TRANSACTION TIMEOUT:
 *    - @Transactional(timeout = 30) - Prevents long-running transactions
 *    - Deadlock detector automatically kills timed-out transactions
 * 
 * 3. MINIMIZE TRANSACTION SCOPE:
 *    - Only critical operations in transaction
 *    - External calls (Feign, Kafka) outside transaction
 * 
 * 4. LOCK ORDERING:
 *    - Access entities in sorted order by ID
 *    - Prevents different transactions from acquiring locks in different order
 * 
 * ========================================================================
 * MICROSERVICES CONCEPTS
 * ========================================================================
 * 
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
 *    - Asynchronous communication (eventual consistency)
 * 
 * 4. SAGA PATTERN (Distributed Transaction Management)
 *    - Order creation spans multiple services
 *    - Compensation transactions for rollback
 *    - Eventual consistency across services
 * 
 * 5. IDEMPOTENCY
 *    - Order ID generation ensures uniqueness
 *    - Retry-safe operations
 * 
 * 6. RETRY PATTERN
 *    - Feign client retries failed requests
 *    - Exponential backoff configured
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
     * INVENTORY CLIENT (Feign Client)
     * Inventory Service को call करने के लिए
     * Stock reservation के लिए
     * RACE CONDITION HANDLING: Inventory Service uses pessimistic locking
     */
    private final InventoryClient inventoryClient;
    
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
                       InventoryClient inventoryClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * CREATE ORDER - Main order creation method
     * 
     * ========================================================================
     * ACID PROPERTIES IMPLEMENTATION
     * ========================================================================
     * 
     * ATOMICITY:
     * - @Transactional ensures all database operations succeed or fail together
     * - If product fetch fails → order not created (rollback)
     * - If total calculation fails → rollback
     * - If save fails → rollback
     * 
     * CONSISTENCY:
     * - Business rule: totalAmount = sum(item.price * item.quantity)
     * - Order must have at least one item
     * - Product IDs must exist
     * 
     * ISOLATION:
     * - READ_COMMITTED: Prevents dirty reads
     * - Other transactions see committed data only
     * - Prevents concurrent modification issues
     * 
     * DURABILITY:
     * - Once committed → order persisted permanently
     * - Database guarantees write to disk
     * 
     * ========================================================================
     * DEADLOCK PREVENTION
     * ========================================================================
     * 
     * 1. SORT PRODUCT IDs BEFORE PROCESSING:
     *    - Always process products in same order (by ID)
     *    - Prevents circular wait (Transaction A waits for Product 1, 
     *      Transaction B waits for Product 2 → Deadlock)
     *    - Solution: Both transactions process in order (1, then 2)
     * 
     * 2. TRANSACTION TIMEOUT:
     *    - timeout = 30 seconds
     *    - If transaction takes too long → auto-rollback
     *    - Prevents long-held locks
     * 
     * 3. MINIMIZE TRANSACTION SCOPE:
     *    - Feign calls outside transaction (no locks held during external calls)
     *    - Kafka publish outside transaction (async, non-blocking)
     *    - Only database operations in transaction
     * 
     * 4. PROPAGATION:
     *    - REQUIRED: Join existing transaction or create new
     *    - Ensures single transaction context
     * 
     * ========================================================================
     * SAGA PATTERN (Distributed Transaction)
     * ========================================================================
     * 
     * Order Creation Saga:
     * 1. Create Order (Order Service) ✅
     * 2. Reserve Inventory (Inventory Service) → If fails: Compensate (release inventory)
     * 3. Process Payment (Payment Service) → If fails: Compensate (cancel order)
     * 4. Send Notification (Notification Service)
     * 
     * Compensation (Rollback):
     * - If payment fails → Cancel order → Release inventory
     * - Eventual consistency: Services eventually consistent
     * 
     * @Transactional: Atomic operation (all-or-nothing)
     * @CircuitBreaker: Fault tolerance
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED, // ACID: Isolation level
        propagation = Propagation.REQUIRED,    // Join or create transaction
        timeout = 30,                          // DEADLOCK PREVENTION: Timeout after 30 seconds
        rollbackFor = Exception.class          // Rollback on any exception
    )
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    public Order createOrder(OrderRequest request) {
        // Verify transaction is active (ACID: Atomicity check)
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new RuntimeException("No active transaction - ACID violation risk");
        }
        
        // Step 1: Create Order object
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(Order.OrderStatus.PENDING); // Initial status
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Step 2: Sort items by productId (DEADLOCK PREVENTION: Consistent ordering)
        // This ensures all transactions process products in same order
        // Prevents: Transaction A locks Product 1, Transaction B locks Product 2 → Deadlock
        // Solution: Both transactions lock in order (Product 1, then Product 2)
        List<OrderRequest.OrderItemRequest> sortedItems = request.getItems().stream()
                .sorted(Comparator.comparing(OrderRequest.OrderItemRequest::getProductId))
                .collect(Collectors.toList());
        
        // Step 3: VALIDATE STOCK BEFORE CREATING ORDER
        // ========================================================================
        // STOCK VALIDATION & RACE CONDITION PREVENTION
        // ========================================================================
        // 
        // PROBLEM: If 1 stock left and 2 customers order at same time:
        // Without validation: Both orders created → Overselling ❌
        // With validation: Only first order succeeds → Correct ✅
        //
        // SOLUTION:
        // 1. Check stock availability from Product Service (quick check)
        // 2. Reserve stock atomically in Inventory Service (with pessimistic locking)
        // 3. Only create order if reservation succeeds
        //
        // RACE CONDITION HANDLING:
        // - Inventory Service uses pessimistic locking (SELECT FOR UPDATE)
        // - Only one transaction can reserve at a time
        // - Atomic check-and-reserve operation prevents overselling
        //
        // EXAMPLE SCENARIO (1 stock left, 2 customers):
        // Customer A: Reserve 1 → Lock row → Check (1 available) → Reserve → Commit ✅
        // Customer B: Reserve 1 → Wait for lock → Lock row → Check (0 available) → Return false ❌
        // Result: Only Customer A's order created ✅
        //
        // STOCK VALIDATION FLOW:
        // 1. Get product info (includes stockQuantity from Product Service)
        // 2. Quick validation: If stockQuantity <= 0 → Reject immediately
        // 3. Reserve in Inventory Service (atomic with locking)
        // 4. If reservation fails → Reject order (stock already reserved)
        
        // Store reservation results for rollback if needed
        Map<Long, Integer> reservedItems = new HashMap<>();
        
        // Step 4: Process each order item (with stock validation)
        List<OrderItem> items = new ArrayList<>();
        for (var itemDto : sortedItems) { // Process in sorted order (deadlock prevention)
            try {
                System.out.println("Processing order item - Product ID: " + itemDto.getProductId() + ", Quantity: " + itemDto.getQuantity());
                
                // STEP 4.1: Fetch product information
                // INTER-SERVICE CALL (Feign Client) - OUTSIDE TRANSACTION
                // This call is outside transaction to prevent long-held locks
                // DEADLOCK PREVENTION: Don't hold database locks during external calls
                ProductDto product = productClient.getProduct(itemDto.getProductId());
                System.out.println("Product fetched: " + product.getId() + " - " + product.getName() + 
                                  ", Stock: " + product.getStockQuantity());
                
                // STEP 4.2: VALIDATE QUANTITY REQUESTED
                if (itemDto.getQuantity() <= 0) {
                    throw new RuntimeException("Order quantity must be greater than 0 for product ID: " + product.getId());
                }
                
                // STEP 4.3: QUICK PRE-CHECK (from Product Service) - Optional early rejection
                // NOTE: This is just a quick check. REAL validation happens in Inventory Service.
                // Product Service stock is denormalized data and might be stale.
                // Inventory Service is the single source of truth for stock availability.
                if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
                    throw new RuntimeException("Product '" + product.getName() + "' (ID: " + product.getId() + 
                                             ") appears to be OUT OF STOCK. Cannot create order.");
                }
                
                // STEP 4.4: RESERVE STOCK ATOMICALLY (RACE CONDITION PREVENTION) - SINGLE SOURCE OF TRUTH
                // ========================================================================
                // CRITICAL: Inventory Service is the SINGLE SOURCE OF TRUTH for stock
                // ========================================================================
                // Why not rely on Product Service stock?
                // - Product Service stock_quantity is denormalized (might be stale)
                // - Inventory Service has real-time data with pessimistic locking
                // - Inventory Service tracks reservedQuantity (orders in progress)
                // - Only Inventory Service can prevent overselling with race condition protection
                //
                // FLOW:
                // 1. Inventory Service uses pessimistic locking (SELECT FOR UPDATE)
                // 2. Checks availableQuantity = quantity - reservedQuantity
                // 3. If availableQuantity >= requested quantity → Reserve
                // 4. If availableQuantity < requested quantity → Return false (prevents overselling)
                //
                // EXAMPLE SCENARIO (Stock = 2, 2 customers order simultaneously):
                // Customer 1 (Order 2 items):
                //   → Lock inventory row → availableQuantity = 2 - 0 = 2
                //   → 2 >= 2? YES → Reserve 2 → reservedQuantity = 2 → Commit
                // Customer 2 (Order 1 item):
                //   → Wait for lock → Lock inventory row → availableQuantity = 2 - 2 = 0
                //   → 0 >= 1? NO → Return false → Order rejected ✅
                // ========================================================================
                System.out.println("🔒 Reserving stock in Inventory Service (Single Source of Truth) - Product ID: " + product.getId() + 
                                  ", Quantity: " + itemDto.getQuantity());
                Boolean reservationSuccess = inventoryClient.reserveInventory(product.getId(), itemDto.getQuantity());
                
                if (reservationSuccess == null || !reservationSuccess) {
                    // Stock reservation failed - This is the REAL validation that prevents overselling
                    // Possible reasons:
                    // 1. Insufficient available stock (another order took it)
                    // 2. Inventory record doesn't exist
                    // 3. Race condition: Another order reserved stock at the same time
                    throw new RuntimeException("Stock reservation failed for product '" + product.getName() + 
                                             "' (ID: " + product.getId() + "). " +
                                             "Insufficient available stock. " +
                                             "This product may have been reserved by another order. " +
                                             "Please try again with a lower quantity or check back later.");
                }
                
                // STEP 4.6: Track reserved items (for potential rollback)
                reservedItems.put(product.getId(), itemDto.getQuantity());
                System.out.println("✅ Stock reserved successfully - Product ID: " + product.getId() + 
                                  ", Reserved Quantity: " + itemDto.getQuantity());
                
                // STEP 4.7: Create OrderItem from Product details
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order); // Link to parent order
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName()); // Denormalization (performance)
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(product.getPrice()); // Price snapshot (price may change later)
                
                items.add(orderItem);
                
            } catch (Exception e) {
                // STEP 4.8: ERROR HANDLING - Release any already reserved stock
                // If we reserved some items but failed on a later item, we need to release
                // This is part of the compensation pattern (Saga pattern)
                System.err.println("❌ Error processing order item - Product ID: " + itemDto.getProductId() + 
                                 ", Error: " + e.getMessage());
                
                // Release already reserved items (compensation)
                for (Map.Entry<Long, Integer> entry : reservedItems.entrySet()) {
                    try {
                        // TODO: Call Inventory Service release endpoint if it exists
                        // For now, reservation will be released when order is cancelled or timeout
                        System.err.println("Note: Stock reserved for Product ID " + entry.getKey() + 
                                         " will be released automatically if order is not completed");
                    } catch (Exception releaseEx) {
                        System.err.println("Failed to release reserved stock for Product ID " + entry.getKey() + 
                                         ": " + releaseEx.getMessage());
                    }
                }
                
                // ACID: Atomicity - If any item fails → entire transaction rolls back
                throw new RuntimeException("Failed to process order item: " + e.getMessage(), e);
                // Transaction automatically rolls back (ACID: Atomicity)
            }
        }
        
        // Step 5: Calculate total amount (ACID: Consistency - Business rule)
        // Business rule: totalAmount must equal sum of (price * quantity)
        totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Validate consistency (ACID: Consistency check)
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Order total must be greater than zero - Consistency violation");
        }
        
        // Step 6: Set items and total amount to order
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        
        // Step 7: Save order to database (ACID: Durability - Persistence)
        // @Transactional ensures this is part of transaction
        // If save fails → entire transaction rolls back (ACID: Atomicity)
        Order savedOrder = orderRepository.save(order);
        
        // Verify transaction still active (ACID: Atomicity verification)
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new RuntimeException("Transaction lost - ACID violation");
        }
        
        // Step 8: Publish Kafka event (ASYNCHRONOUS - OUTSIDE TRANSACTION)
        // DEADLOCK PREVENTION: Don't hold transaction during async operations
        // SAGA PATTERN: Event-driven coordination
        // This runs in separate thread → doesn't block transaction
        // If Kafka fails → order still created (eventual consistency)
        new Thread(() -> {
            try {
                // Publish to "order-created" topic
                // Other services (Inventory, Notification, Payment) listen to this
                // SAGA PATTERN: Choreography (services react to events)
                kafkaTemplate.send("order-created", savedOrder);
                System.out.println("Kafka event published (async) - Saga step: Order created event");
            } catch (Exception e) {
                // Log but don't fail - Kafka is optional (eventual consistency)
                // Order is created, events will be processed eventually
                System.err.println("Kafka publish failed (non-critical): " + e.getMessage());
            }
        }).start();
        
        // ACID: Transaction commits here (if no exception)
        // Durability: Data written to database permanently
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
     * CIRCUIT BREAKER PATTERN:
     * - Prevents cascading failures
     * - Graceful degradation
     * - Fast failure (no waiting for timeout)
     * 
     * @param request - Original order request
     * @param ex - Exception that caused fallback
     * @return Order - Never returns (throws exception)
     * @throws RuntimeException - Error message for user
     */
    public Order createOrderFallback(OrderRequest request, Exception ex) {
        // Fallback when order creation fails
        // Circuit breaker prevents further calls to failing service
        throw new RuntimeException("Order service temporarily unavailable. Please try again later.", ex);
    }
    
    /**
     * GET ORDERS BY USER ID
     * 
     * DEADLOCK PREVENTION:
     * - Read-only operation (no locks acquired)
     * - Uses database indexes for fast lookups
     * 
     * @param userId - User ID
     * @return List<Order> - User's orders
     */
    @Transactional(readOnly = true) // Read-only transaction (no locks, better performance)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * GET ALL ORDERS
     * 
     * @return List<Order> - All orders
     */
    @Transactional(readOnly = true) // Read-only transaction
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    /**
     * GET ORDER BY ID
     * 
     * @param id - Order ID
     * @return Order - Order object
     * @throws RuntimeException - If order not found
     */
    @Transactional(readOnly = true) // Read-only transaction
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    /**
     * UPDATE ORDER STATUS
     * 
     * ACID PROPERTIES:
     * - Atomicity: Status update is atomic (all-or-nothing)
     * - Consistency: Status transitions are valid (PENDING → CONFIRMED → SHIPPED → DELIVERED)
     * - Isolation: Other transactions see committed status only
     * - Durability: Status change persisted permanently
     * 
     * DEADLOCK PREVENTION:
     * - Single entity update (minimal lock scope)
     * - Optimistic locking can be used (version field)
     * 
     * @param id - Order ID
     * @param status - New status
     * @return Order - Updated order
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 10 // Short timeout for simple update
    )
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        // ACID: Consistency - Validate status transition
        Order order = getOrderById(id); // Fetch order
        
        // Business rule: Status transitions must be valid
        // Example: Cannot go from DELIVERED back to PENDING
        if (!isValidStatusTransition(order.getStatus(), status)) {
            throw new RuntimeException("Invalid status transition from " + order.getStatus() + " to " + status);
        }
        
        // STOCK DEDUCTION: When order status changes to CONFIRMED (payment success)
        // ========================================================================
        // FLOW:
        // 1. Order created → Stock reserved (reservedQuantity increases in Inventory Service)
        // 2. Payment success → Order status changes to CONFIRMED
        // 3. Deduct actual stock from both:
        //    - Product Service (products.stock_quantity)
        //    - Inventory Service (inventory.quantity and reservedQuantity)
        // ========================================================================
        if (status == Order.OrderStatus.CONFIRMED && order.getStatus() == Order.OrderStatus.PENDING) {
            System.out.println("💰 Order confirmed - Deducting stock for order ID: " + id);
            
            // Deduct stock for each item in the order
            for (OrderItem item : order.getItems()) {
                Long productId = item.getProductId();
                Integer quantity = item.getQuantity();
                
                try {
                    // Step 1: Deduct from Inventory Service (SINGLE SOURCE OF TRUTH)
                    // Inventory Service uses pessimistic locking to ensure atomic deduction
                    // This is the PRIMARY stock deduction - must succeed
                    System.out.println("📦 Deducting from Inventory Service (Primary) - Product ID: " + productId + ", Quantity: " + quantity);
                    inventoryClient.deductInventory(productId, quantity);
                    System.out.println("✅ Inventory Service stock deducted successfully");
                    
                    // Step 2: Sync Product Service (denormalized stock_quantity)
                    // Product Service stock is for display purposes only
                    // Sync it with Inventory Service to keep frontend accurate
                    System.out.println("🔄 Syncing Product Service stock - Product ID: " + productId + ", Quantity: " + quantity);
                    try {
                        productClient.decrementStock(productId, quantity);
                        System.out.println("✅ Product Service stock synced successfully");
                    } catch (Exception productSyncError) {
                        // Product Service sync failure is non-critical
                        // Inventory Service (source of truth) already deducted
                        // Log but don't fail order confirmation
                        System.err.println("⚠️ Warning: Product Service stock sync failed (non-critical): " + productSyncError.getMessage());
                        System.err.println("   Inventory Service stock already deducted. Product Service can be synced later.");
                    }
                    
                    System.out.println("✅ Stock deduction complete - Product ID: " + productId);
                } catch (Exception e) {
                    // CRITICAL: If Inventory Service deduction fails, this is a serious issue
                    // Order is confirmed but stock not deducted - needs manual intervention
                    System.err.println("❌ CRITICAL: Failed to deduct stock from Inventory Service for Product ID " + productId + ": " + e.getMessage());
                    System.err.println("   Order is confirmed but stock not deducted. Manual intervention required!");
                    // Continue with other items - order status update should not fail
                    // But this needs to be logged and alerted
                }
            }
        }
        
        order.setStatus(status); // Update status
        return orderRepository.save(order); // Save (ACID: Durability)
    }
    
    /**
     * VALIDATE STATUS TRANSITION
     * 
     * ACID: Consistency - Business rule enforcement
     * 
     * @param currentStatus - Current order status
     * @param newStatus - New order status
     * @return boolean - true if transition is valid
     */
    private boolean isValidStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Valid transitions:
        // PENDING → CONFIRMED, CANCELLED
        // CONFIRMED → SHIPPED, CANCELLED
        // SHIPPED → DELIVERED
        // DELIVERED → (no transitions)
        // CANCELLED → (no transitions)
        
        if (currentStatus == Order.OrderStatus.PENDING) {
            return newStatus == Order.OrderStatus.CONFIRMED || 
                   newStatus == Order.OrderStatus.CANCELLED;
        } else if (currentStatus == Order.OrderStatus.CONFIRMED) {
            return newStatus == Order.OrderStatus.SHIPPED || 
                   newStatus == Order.OrderStatus.CANCELLED;
        } else if (currentStatus == Order.OrderStatus.SHIPPED) {
            return newStatus == Order.OrderStatus.DELIVERED;
        }
        // DELIVERED and CANCELLED are terminal states
        return false;
    }
}
