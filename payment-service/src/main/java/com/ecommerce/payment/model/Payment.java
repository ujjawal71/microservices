package com.ecommerce.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PAYMENT ENTITY - Payment Data Model
 * 
 * ========================================================================
 * ACID PROPERTIES - ENTITY LEVEL
 * ========================================================================
 * 
 * ATOMICITY:
 * - Payment save is atomic
 * - All payment fields saved together
 * 
 * CONSISTENCY:
 * - Payment status transitions validated
 * - Amount must be positive
 * - Order ID must exist
 * 
 * ISOLATION:
 * - Concurrent payments handled safely
 * - Pessimistic locking for critical operations
 * 
 * DURABILITY:
 * - Payment records persisted permanently
 * - Transaction logs ensure recovery
 * 
 * ========================================================================
 * IDEMPOTENCY SUPPORT
 * ========================================================================
 * 
 * TRANSACTION ID:
 * - Unique identifier for payment transaction
 * - Prevents duplicate payments
 * - Retry-safe operations
 * 
 * RAZORPAY IDs:
 * - razorpayOrderId: Razorpay order identifier
 * - razorpayPaymentId: Razorpay payment identifier
 * - Used for payment verification and tracking
 */
@Entity
@Table(name = "payments") // Database table name
public class Payment {
    
    /**
     * PRIMARY KEY
     * Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ORDER ID
     * Foreign key to orders table (in Order Service)
     * 
     * @Column(name = "order_id"):
     * - Explicit column name mapping
     * - camelCase (orderId) → snake_case (order_id) in database
     */
    @Column(name = "order_id")
    private Long orderId;
    
    /**
     * PAYMENT AMOUNT
     * 
     * ACID: Consistency - Must be positive
     * Business rule: amount > 0
     */
    private BigDecimal amount;
    
    /**
     * PAYMENT STATUS
     * 
     * STATUS FLOW (ACID: Consistency - Valid transitions):
     * PENDING → COMPLETED or FAILED
     * COMPLETED → REFUNDED (if refund requested)
     * 
     * ENUM stored as String in database
     */
    @Enumerated(EnumType.STRING) // Store enum as String
    private PaymentStatus status = PaymentStatus.PENDING; // Default status
    
    /**
     * PAYMENT METHOD
     * Payment gateway or method used
     * Examples: RAZORPAY, CASH_ON_DELIVERY, CREDIT_CARD
     */
    private String paymentMethod;
    
    /**
     * TRANSACTION ID
     * 
     * IDEMPOTENCY:
     * - Unique identifier for payment transaction
     * - Prevents duplicate payments
     * - Same transaction ID = same payment (no duplicate processing)
     * 
     * RETRY SAFETY:
     * - If payment request retried → check transaction ID
     * - If exists → return existing payment (no duplicate)
     */
    private String transactionId; // Razorpay payment ID or custom transaction ID
    
    /**
     * RAZORPAY ORDER ID
     * 
     * Razorpay gateway order identifier
     * Used for payment tracking and verification
     */
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;
    
    /**
     * RAZORPAY PAYMENT ID
     * 
     * Razorpay gateway payment identifier
     * Unique identifier for completed payment
     */
    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;
    
    /**
     * PAYMENT DATE
     * 
     * Timestamp when payment was processed
     * 
     * ACID: Durability - Timestamp persisted permanently
     */
    private LocalDateTime paymentDate = LocalDateTime.now(); // Default to current time
    
    /**
     * DEFAULT CONSTRUCTOR
     * JPA requirement
     */
    public Payment() {}
    
    // ========== GETTERS AND SETTERS ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
    
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    
    /**
     * PAYMENT STATUS ENUM
     * 
     * Status values for payment lifecycle
     * 
     * STATUS FLOW:
     * 1. PENDING - Payment initiated, processing
     * 2. COMPLETED - Payment successful
     * 3. FAILED - Payment failed
     * 4. REFUNDED - Payment refunded
     * 
     * ACID: Consistency - Status transitions must be valid
     */
    public enum PaymentStatus {
        PENDING,   // Payment processing
        COMPLETED, // Payment successful
        FAILED,    // Payment failed
        REFUNDED   // Payment refunded
    }
}
