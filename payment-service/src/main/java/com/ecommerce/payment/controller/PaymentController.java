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

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final RazorpayService razorpayService;
    private final OrderClient orderClient;

    public PaymentController(PaymentService paymentService, RazorpayService razorpayService, OrderClient orderClient) {
        this.paymentService = paymentService;
        this.razorpayService = razorpayService;
        this.orderClient = orderClient;
    }
    
    // Specific routes must come before generic {id} route
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createRazorpayOrder(
            @RequestParam Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "INR") String currency) {
        try {
            String receiptId = "receipt_" + orderId + "_" + UUID.randomUUID().toString().substring(0, 8);
            Order razorpayOrder = razorpayService.createOrder(amount, currency, receiptId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", razorpayOrder.get("id"));
            response.put("amount", razorpayOrder.get("amount"));
            response.put("currency", razorpayOrder.get("currency"));
            response.put("receipt", razorpayOrder.get("receipt"));
            response.put("orderId", orderId);
            
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create Razorpay order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestParam Long orderId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature,
            @RequestParam BigDecimal amount) {
        try {
            boolean isValid = razorpayService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);
            
            Map<String, Object> response = new HashMap<>();
            if (isValid) {
                // Create payment record with Razorpay details
                Payment payment = new Payment();
                payment.setOrderId(orderId);
                payment.setAmount(amount);
                payment.setPaymentMethod("RAZORPAY");
                payment.setRazorpayOrderId(razorpayOrderId);
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setTransactionId(razorpayPaymentId); // Keep for backward compatibility
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                
                System.out.println("Saving payment with: orderId=" + orderId + ", razorpayOrderId=" + razorpayOrderId + ", razorpayPaymentId=" + razorpayPaymentId);
                
                Payment savedPayment = paymentService.savePayment(payment);
                
                System.out.println("Payment saved successfully: ID=" + savedPayment.getId() + ", Order ID=" + savedPayment.getOrderId() + ", Razorpay Order ID=" + savedPayment.getRazorpayOrderId() + ", Razorpay Payment ID=" + savedPayment.getRazorpayPaymentId());
                
                // Update order status to CONFIRMED after successful payment
                try {
                    System.out.println("Updating order status to CONFIRMED for order ID: " + orderId);
                    orderClient.updateOrderStatus(orderId, "CONFIRMED");
                    System.out.println("Order status updated successfully");
                } catch (Exception e) {
                    System.err.println("Failed to update order status (non-critical): " + e.getMessage());
                    // Don't fail payment if order status update fails - log and continue
                }
                
                response.put("success", true);
                response.put("message", "Payment verified and processed successfully");
                response.put("payment", savedPayment);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Payment verification failed");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error verifying payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }
    
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
    
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

