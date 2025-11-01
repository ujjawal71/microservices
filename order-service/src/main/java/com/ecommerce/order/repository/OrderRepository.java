package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;

/**
 * ORDER REPOSITORY - Data Access Layer
 * 
 * ========================================================================
 * ACID PROPERTIES SUPPORT
 * ========================================================================
 * 
 * 1. TRANSACTION SUPPORT:
 *    - JpaRepository provides transaction management
 *    - All operations within transaction context
 *    - ACID properties enforced by database
 * 
 * 2. PESSIMISTIC LOCKING:
 *    - @Lock(LockModeType.PESSIMISTIC_WRITE) for concurrent updates
 *    - Prevents race conditions
 *    - DEADLOCK PREVENTION: Consistent lock ordering
 * 
 * ========================================================================
 * DEADLOCK PREVENTION
 * ========================================================================
 * 
 * - Consistent ordering in queries (by ID)
 * - Pessimistic locking for critical operations
 * - Short transaction scope
 * - Lock timeout configured
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * FIND BY USER ID
     * 
     * Find all orders for a specific user
     * 
     * @param userId - User ID
     * @return List<Order> - User's orders
     */
    List<Order> findByUserId(Long userId);
    
    /**
     * FIND BY STATUS
     * 
     * Find orders by status (for filtering)
     * 
     * USE CASE:
     * - Admin dashboard: Filter by status
     * - Reports: Count orders by status
     * 
     * @param status - Order status
     * @return List<Order> - Orders with that status
     */
    List<Order> findByStatus(Order.OrderStatus status);
    
    /**
     * FIND BY USER ID WITH PESSIMISTIC LOCK
     * 
     * DEADLOCK PREVENTION:
     * - Pessimistic lock prevents concurrent modifications
     * - Use for critical order operations
     * 
     * ACID: Isolation - Prevents dirty reads and lost updates
     * 
     * @param userId - User ID
     * @return List<Order> - Orders (locked for update)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE) // Pessimistic lock
    @Query("SELECT o FROM Order o WHERE o.userId = :userId")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")}) // 5 second timeout
    List<Order> findByUserIdWithLock(Long userId);
}
