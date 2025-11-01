package com.ecommerce.payment.controller;

import com.ecommerce.payment.client.OrderClient;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.service.PaymentService;
import com.ecommerce.payment.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PAYMENT CONTROLLER - Payment Gateway Integration REST API
 * 
 * PURPOSE (उद्देश्य):
 * - Razorpay payment gateway integration
 * - Payment processing और verification
 * - Inter-service communication (Order Service update)
 * 
 * RAZORPAY PAYMENT FLOW:
 * 1. Frontend → POST /api/payments/create-order → Razorpay order create
 * 2. Frontend → Razorpay checkout modal → User pays
 * 3. Frontend → POST /api/payments/verify → Verify payment signature
 * 4. Payment Service → Order Service (update status to CONFIRMED)
 * 
 * INTER-SERVICE COMMUNICATION:
 * Payment Service → Order Service (Feign Client)
 * - Payment success पर order status update करता है
 * - Order Service को notify करता है
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    /**
     * PAYMENT SERVICE
     * Payment database operations के लिए
     */
    private final PaymentService paymentService;
    
    /**
     * RAZORPAY SERVICE
     * Razorpay API calls के लिए
     */
    private final RazorpayService razorpayService;
    
    /**
     * ORDER CLIENT (Feign Client)
     * Order Service को call करने के लिए
     * Payment success पर order status update करने के लिए
     */
    private final OrderClient orderClient;
    
    /**
     * CONSTRUCTOR INJECTION
     */
    public PaymentController(PaymentService paymentService, RazorpayService razorpayService, OrderClient orderClient) {
        this.paymentService = paymentService;
        this.razorpayService = razorpayService;
        this.orderClient = orderClient;
    }
    
    /**
     * CREATE RAZORPAY ORDER
     * 
     * Razorpay payment gateway में order create करता है
     * Frontend को Razorpay order ID return करता है
     * 
     * FLOW:
     * 1. Receive orderId, amount, currency from frontend
     * 2. Generate unique receipt ID
     * 3. Call Razorpay API to create order
     * 4. Return Razorpay order details to frontend
     * 
     * USE CASE:
     * - Checkout page पर user "Pay Now" click करता है
     * - Frontend यह endpoint call करता है
     * - Razorpay order ID मिलता है
     * - Razorpay checkout modal open होता है
     * 
     * ROUTE ORDER IMPORTANT:
     * Specific routes (/create-order, /verify) must come before generic /{id}
     * Otherwise Spring will match /{id} first!
     * 
     * @param orderId - Our application's order ID
     * @param amount - Payment amount
     * @param currency - Currency code (default: INR)
     * @return ResponseEntity - Razorpay order details or error
     */
    // Specific routes must come before generic {id} route
    @PostMapping("/create-order") // Must come before @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> createRazorpayOrder(
            @RequestParam Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "INR") String currency) {
        try {
            // Generate unique receipt ID for Razorpay
            // Format: receipt_{orderId}_{randomUUID}
            String receiptId = "receipt_" + orderId + "_" + UUID.randomUUID().toString().substring(0, 8);
            
            // Call Razorpay Service to create order
            Order razorpayOrder = razorpayService.createOrder(amount, currency, receiptId);
            
            // Build response for frontend
            Map<String, Object> response = new HashMap<>();
            response.put("id", razorpayOrder.get("id")); // Razorpay order ID (needed for checkout)
            response.put("amount", razorpayOrder.get("amount")); // Amount in paise
            response.put("currency", razorpayOrder.get("currency")); // Currency code
            response.put("receipt", razorpayOrder.get("receipt")); // Receipt ID
            response.put("orderId", orderId); // Our application's order ID
            
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            // Razorpay API error
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create Razorpay order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * VERIFY PAYMENT
     * 
     * Razorpay payment verification करता है
     * Payment success पर database में save करता है
     * Order status को CONFIRMED update करता है
     * 
     * FLOW:
     * 1. Receive payment details from frontend (Razorpay response)
     * 2. Verify Razorpay signature (security check)
     * 3. If valid → Save payment to database
     * 4. Update order status to CONFIRMED (Order Service call)
     * 5. Return success response
     * 
     * SECURITY:
     * - Signature verification: Payment tampering detect करता है
     * - HMAC SHA256 algorithm use होता है
     * - Razorpay secret key से verify होता है
     * 
     * INTER-SERVICE CALL:
     * Payment Service → Order Service (updateOrderStatus)
     * - Payment success पर order status CONFIRMED हो जाता है
     * 
     * @param orderId - Our application's order ID
     * @param razorpayOrderId - Razorpay order ID
     * @param razorpayPaymentId - Razorpay payment ID
     * @param razorpaySignature - Razorpay signature (for verification)
     * @param amount - Payment amount
     * @return ResponseEntity - Success or failure response
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestParam Long orderId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature,
            @RequestParam BigDecimal amount) {
        try {
            // Step 1: Verify Razorpay payment signature
            boolean isValid = razorpayService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);
            
            Map<String, Object> response = new HashMap<>();
            if (isValid) {
                // Step 2: Payment signature verified → Create payment record
                Payment payment = new Payment();
                payment.setOrderId(orderId);
                payment.setAmount(amount);
                payment.setPaymentMethod("RAZORPAY");
                payment.setRazorpayOrderId(razorpayOrderId); // Razorpay order ID
                payment.setRazorpayPaymentId(razorpayPaymentId); // Razorpay payment ID
                payment.setTransactionId(razorpayPaymentId); // Transaction ID (backward compatibility)
                payment.setStatus(Payment.PaymentStatus.COMPLETED); // Payment status
                
                System.out.println("Saving payment with: orderId=" + orderId + ", razorpayOrderId=" + razorpayOrderId + ", razorpayPaymentId=" + razorpayPaymentId);
                
                // Step 3: Save payment to database
                Payment savedPayment = paymentService.savePayment(payment);
                
                System.out.println("Payment saved successfully: ID=" + savedPayment.getId() + ", Order ID=" + savedPayment.getOrderId() + ", Razorpay Order ID=" + savedPayment.getRazorpayOrderId() + ", Razorpay Payment ID=" + savedPayment.getRazorpayPaymentId());
                
                // Step 4: Update order status to CONFIRMED (Inter-service call)
                // Payment Service → Order Service (Feign Client)
                try {
                    System.out.println("Updating order status to CONFIRMED for order ID: " + orderId);
                    orderClient.updateOrderStatus(orderId, "CONFIRMED"); // Feign Client call
                    System.out.println("Order status updated successfully");
                } catch (Exception e) {
                    // Non-critical: If order update fails, payment is still saved
                    // Log error but don't fail payment processing
                    System.err.println("Failed to update order status (non-critical): " + e.getMessage());
                    // Don't fail payment if order status update fails - log and continue
                }
                
                // Step 5: Return success response
                response.put("success", true);
                response.put("message", "Payment verified and processed successfully");
                response.put("payment", savedPayment);
                return ResponseEntity.ok(response);
            } else {
                // Payment signature verification failed
                response.put("success", false);
                response.put("message", "Payment verification failed");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            // Unexpected error
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error verifying payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * GET PAYMENTS BY ORDER ID
     * 
     * Order के लिए सभी payments fetch करता है
     * 
     * USE CASE:
     * - Admin panel में order के payments show करने के लिए
     * - Payment history display करने के लिए
     * 
     * @param orderId - Order ID
     * @return ResponseEntity<List<Payment>> - Payments for that order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }
    
    /**
     * PROCESS PAYMENT (Legacy endpoint - for direct payment processing)
     * 
     * @param orderId - Order ID
     * @param amount - Payment amount
     * @param paymentMethod - Payment method
     * @return ResponseEntity<Payment> - Created payment
     */
    @PostMapping
    public ResponseEntity<Payment> processPayment(
            @RequestParam Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(paymentService.processPayment(orderId, amount, paymentMethod));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * GET PAYMENT BY ID
     * 
     * Specific payment details fetch करता है
     * 
     * @param id - Payment ID
     * @return ResponseEntity<Payment> - Payment details or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
