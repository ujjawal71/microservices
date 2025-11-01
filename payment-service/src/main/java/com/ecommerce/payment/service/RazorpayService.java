package com.ecommerce.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * RAZORPAY SERVICE - Razorpay Payment Gateway Integration
 * 
 * PURPOSE (उद्देश्य):
 * - Razorpay API calls encapsulate करना
 * - Payment order creation
 * - Payment signature verification (security)
 * 
 * RAZORPAY CONCEPT:
 * - Third-party payment gateway (India's popular gateway)
 * - Handles payment processing securely
 * - Provides checkout modal for customers
 * - Returns payment verification signature
 * 
 * SECURITY:
 * - HMAC SHA256 signature verification
 * - Prevents payment tampering
 * - Secret key stored in application.yml
 */
@Service
public class RazorpayService {
    
    /**
     * RAZORPAY CLIENT
     * Razorpay API calls के लिए (configured with API keys)
     */
    private final RazorpayClient razorpayClient;
    
    /**
     * RAZORPAY SECRET KEY
     * Payment signature verification के लिए
     * 
     * @Value: application.yml से inject होता है
     * Default value provided (for fallback)
     */
    @Value("${razorpay.key.secret:MWXd0wcJMMLWFJsl9crZo1lz}")
    private String keySecret;
    
    /**
     * CONSTRUCTOR INJECTION
     */
    public RazorpayService(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }
    
    /**
     * CREATE RAZORPAY ORDER
     * 
     * Razorpay payment gateway में order create करता है
     * 
     * FLOW:
     * 1. Amount convert करो (rupees → paise)
     * 2. Razorpay API call करो
     * 3. Razorpay order object return करो
     * 
     * AMOUNT CONVERSION:
     * - Razorpay uses smallest currency unit
     * - INR: Rupees → Paise (multiply by 100)
     * - USD: Dollars → Cents (multiply by 100)
     * 
     * @param amount - Payment amount (in rupees/dollars)
     * @param currency - Currency code (INR, USD, etc.)
     * @param receiptId - Unique receipt identifier
     * @return Order - Razorpay order object
     * @throws RazorpayException - If Razorpay API call fails
     */
    public Order createOrder(BigDecimal amount, String currency, String receiptId) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        
        // Convert amount to paise (Razorpay uses smallest currency unit)
        // Example: ₹100.50 → 10050 paise
        // For USD, convert to cents; for INR, convert to paise
        long amountInPaise = amount.multiply(new BigDecimal("100")).longValue();
        
        // Build Razorpay order request
        orderRequest.put("amount", amountInPaise); // Amount in paise
        orderRequest.put("currency", currency != null ? currency : "INR"); // Currency (default: INR)
        orderRequest.put("receipt", receiptId); // Unique receipt ID
        
        // Call Razorpay API to create order
        Order order = razorpayClient.orders.create(orderRequest);
        return order;
    }
    
    /**
     * VERIFY PAYMENT SIGNATURE
     * 
     * Razorpay payment signature verify करता है
     * Security check - payment tampering detect करने के लिए
     * 
     * FLOW:
     * 1. Create data string: razorpayOrderId + "|" + razorpayPaymentId
     * 2. Calculate HMAC SHA256 signature using secret key
     * 3. Compare generated signature with received signature
     * 4. If match → Payment is valid ✅
     * 
     * WHY SIGNATURE VERIFICATION:
     * - Razorpay sends signature with payment response
     * - We verify signature using our secret key
     * - If signature matches → Payment is genuine (not tampered)
     * - Prevents payment fraud
     * 
     * @param razorpayOrderId - Razorpay order ID
     * @param razorpayPaymentId - Razorpay payment ID
     * @param razorpaySignature - Razorpay signature (received from Razorpay)
     * @return boolean - true if signature valid, false otherwise
     */
    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            // Create data string for signature verification
            // Format: orderId|paymentId
            String data = razorpayOrderId + "|" + razorpayPaymentId;
            
            // Calculate HMAC SHA256 signature using our secret key
            String generatedSignature = calculateHMAC(data, keySecret);
            
            // Compare generated signature with received signature
            // If match → Payment is valid
            return generatedSignature.equals(razorpaySignature);
        } catch (Exception e) {
            // Error in signature verification
            e.printStackTrace();
            return false; // Signature verification failed
        }
    }
    
    /**
     * CALCULATE HMAC SHA256 SIGNATURE
     * 
     * HMAC (Hash-based Message Authentication Code) calculate करता है
     * Security के लिए use होता है
     * 
     * ALGORITHM:
     * - HMAC SHA256
     * - Uses secret key for signing
     * - Converts result to hexadecimal string
     * 
     * @param data - Data to sign (orderId|paymentId)
     * @param secret - Secret key for signing
     * @return String - Hexadecimal HMAC signature
     * 
     * SECURITY:
     * - HMAC is one-way function (cannot reverse)
     * - Same input + same secret = same output
     * - Different input or secret = different output
     */
    private String calculateHMAC(String data, String secret) {
        try {
            // Initialize HMAC SHA256 algorithm
            Mac mac = Mac.getInstance("HmacSHA256");
            
            // Create secret key spec from string
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            // Calculate HMAC hash
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b); // Convert byte to hex
                if (hex.length() == 1) {
                    hexString.append('0'); // Pad with 0 if single digit
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC", e);
        }
    }
}
