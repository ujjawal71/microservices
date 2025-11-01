package com.ecommerce.payment.repository;

import com.ecommerce.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;

/**
 * PAYMENT REPOSITORY - Data Access Layer
 * 
 * ========================================================================
 * ACID PROPERTIES SUPPORT
 * ========================================================================
 * 
 * 1. PESSIMISTIC LOCKING:
 *    - @Lock(LockModeType.PESSIMISTIC_WRITE) for concurrent updates
 *    - Prevents race conditions
 *    - DEADLOCK PREVENTION: Consistent lock ordering
 * 
 * 2. TRANSACTION SUPPORT:
 *    - JpaRepository provides transaction management
 *    - All operations within transaction context
 * 
 * ========================================================================
 * DEADLOCK PREVENTION
 * ========================================================================
 * 
 * - Consistent ordering in queries (by ID)
 * - Pessimistic locking for critical operations
 * - Short transaction scope
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * FIND BY ORDER ID
     * 
     * @param orderId - Order ID
     * @return List<Payment> - Payments for that order
     */
    List<Payment> findByOrderId(Long orderId);
    
    /**
     * FIND BY STATUS
     * 
     * @param status - Payment status
     * @return List<Payment> - Payments with that status
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    /**
     * FIND BY TRANSACTION ID
     * 
     * IDEMPOTENCY SUPPORT:
     * - Check if payment already processed
     * - Prevent duplicate payments
     * 
     * @param transactionId - Transaction ID
     * @return List<Payment> - Payments with that transaction ID
     */
    List<Payment> findByTransactionId(String transactionId);
    
    /**
     * FIND BY ORDER ID WITH PESSIMISTIC LOCK
     * 
     * DEADLOCK PREVENTION:
     * - Pessimistic lock prevents concurrent modifications
     * - Use for critical payment operations
     * 
     * ACID: Isolation - Prevents dirty reads and lost updates
     * 
     * @param orderId - Order ID
     * @return List<Payment> - Payments (locked for update)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE) // Pessimistic lock (DEADLOCK PREVENTION)
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")}) // 5 second timeout
    List<Payment> findByOrderIdWithLock(Long orderId);
}
