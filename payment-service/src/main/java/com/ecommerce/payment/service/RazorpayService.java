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

@Service
public class RazorpayService {
    
    private final RazorpayClient razorpayClient;
    
    @Value("${razorpay.key.secret:MWXd0wcJMMLWFJsl9crZo1lz}")
    private String keySecret;
    
    public RazorpayService(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }
    
    public Order createOrder(BigDecimal amount, String currency, String receiptId) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        
        // Convert amount to paise (Razorpay uses smallest currency unit)
        // For USD, convert to cents; for INR, convert to paise
        long amountInPaise = amount.multiply(new BigDecimal("100")).longValue();
        
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", currency != null ? currency : "INR");
        orderRequest.put("receipt", receiptId);
        
        Order order = razorpayClient.orders.create(orderRequest);
        return order;
    }
    
    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            String data = razorpayOrderId + "|" + razorpayPaymentId;
            String generatedSignature = calculateHMAC(data, keySecret);
            return generatedSignature.equals(razorpaySignature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private String calculateHMAC(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC", e);
        }
    }
}

