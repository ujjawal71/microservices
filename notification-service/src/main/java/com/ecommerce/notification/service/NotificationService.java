package com.ecommerce.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    @KafkaListener(topics = "order-created", groupId = "notification-group")
    public void handleOrderCreated(Object order) {
        System.out.println("Notification: Order created - " + order);
        // Send email/SMS notification
    }
    
    @KafkaListener(topics = "payment-completed", groupId = "notification-group")
    public void handlePaymentCompleted(Object payment) {
        System.out.println("Notification: Payment completed - " + payment);
        // Send confirmation notification
    }
}

