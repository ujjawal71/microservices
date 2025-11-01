package com.ecommerce.payment.service;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public PaymentService(PaymentRepository paymentRepository, 
                         KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Transactional
    public Payment processPayment(Long orderId, BigDecimal amount, String paymentMethod) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        
        // Simulate payment processing
        try {
            // In real scenario, integrate with payment gateway
            Thread.sleep(1000);
            
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            
            Payment savedPayment = paymentRepository.save(payment);
            
            // Publish payment completed event
            kafkaTemplate.send("payment-completed", savedPayment);
            
            return savedPayment;
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed", e);
        }
    }
    
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
    
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}
