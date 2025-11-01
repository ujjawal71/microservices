package com.ecommerce.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * NOTIFICATION SERVICE - Event-Driven Notification Handler
 * 
 * ========================================================================
 * EVENT-DRIVEN ARCHITECTURE
 * ========================================================================
 * 
 * PURPOSE:
 * - Listens to Kafka events
 * - Sends notifications (Email, SMS) based on events
 * - Asynchronous processing
 * 
 * KAFKA CONSUMER:
 * - @KafkaListener subscribes to topics
 * - Automatically receives events when published
 * - Processes events asynchronously
 * 
 * ========================================================================
 * MICROSERVICES CONCEPTS
 * ========================================================================
 * 
 * 1. EVENT-DRIVEN COMMUNICATION:
 *    - Other services publish events
 *    - This service reacts to events
 *    - Loose coupling (no direct service calls)
 * 
 * 2. ASYNCHRONOUS PROCESSING:
 *    - Events processed asynchronously
 *    - Non-blocking
 *    - Better performance
 * 
 * 3. SCALABILITY:
 *    - Multiple instances can process events in parallel
 *    - Kafka handles load balancing
 * 
 * 4. RESILIENCE:
 *    - If service down → events queued
 *    - Service can catch up later
 * 
 * ========================================================================
 * EVENT FLOW
 * ========================================================================
 * 
 * Order Created Event:
 * Order Service → Publishes "order-created" → Notification Service receives → Sends email
 * 
 * Payment Completed Event:
 * Payment Service → Publishes "payment-completed" → Notification Service receives → Sends confirmation
 */
@Service
public class NotificationService {
    
    /**
     * HANDLE ORDER CREATED EVENT
     * 
     * KAFKA LISTENER:
     * - @KafkaListener subscribes to "order-created" topic
     * - Automatically receives events when Order Service publishes
     * - groupId: "notification-group" (consumer group)
     * 
     * CONSUMER GROUP:
     * - Multiple instances can be in same group
     * - Kafka distributes events across instances
     * - Load balancing
     * 
     * EVENT-DRIVEN:
     * - Reactive: Service reacts to events
     * - Asynchronous: Non-blocking processing
     * 
     * USE CASE:
     * - Send order confirmation email to customer
     * - Send notification to admin
     * 
     * @param order - Order object (deserialized from JSON)
     */
    @KafkaListener(topics = "order-created", groupId = "notification-group")
    public void handleOrderCreated(Object order) {
        System.out.println("Notification: Order created - " + order);
        
        // In production, implement actual notification logic:
        // 1. Extract order details
        // 2. Get user email from User Service
        // 3. Send email/SMS notification
        // 4. Log notification status
        
        // Example:
        // emailService.sendOrderConfirmation(order);
        // smsService.sendOrderSMS(order);
    }
    
    /**
     * HANDLE PAYMENT COMPLETED EVENT
     * 
     * KAFKA LISTENER:
     * - Subscribes to "payment-completed" topic
     * - Receives events from Payment Service
     * 
     * USE CASE:
     * - Send payment confirmation email
     * - Send receipt to customer
     * - Notify admin of successful payment
     * 
     * @param payment - Payment object (deserialized from JSON)
     */
    @KafkaListener(topics = "payment-completed", groupId = "notification-group")
    public void handlePaymentCompleted(Object payment) {
        System.out.println("Notification: Payment completed - " + payment);
        
        // In production, implement actual notification logic:
        // 1. Extract payment details
        // 2. Get user email from User Service
        // 3. Send payment confirmation email
        // 4. Send receipt
        // 5. Log notification status
        
        // Example:
        // emailService.sendPaymentConfirmation(payment);
        // emailService.sendReceipt(payment);
    }
}
