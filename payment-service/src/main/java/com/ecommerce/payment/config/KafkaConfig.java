package com.ecommerce.payment.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * KAFKA CONFIGURATION - Payment Service Event Publishing
 * 
 * ========================================================================
 * EVENT-DRIVEN ARCHITECTURE
 * ========================================================================
 * 
 * PAYMENT EVENTS:
 * - Payment completed → Published to "payment-completed" topic
 * - Consumers: Notification Service, Order Service (optional)
 * 
 * ASYNCHRONOUS COMMUNICATION:
 * - Payment Service doesn't wait for consumers
 * - Event published → Service continues
 * - Consumers process events independently
 * 
 * ========================================================================
 * SAGA PATTERN (Distributed Transaction)
 * ========================================================================
 * 
 * Payment Saga:
 * 1. Order created → Order Service publishes "order-created"
 * 2. Payment processed → Payment Service publishes "payment-completed"
 * 3. Order confirmed → Order status updated
 * 4. Notification sent → Notification Service consumes event
 * 
 * EVENTUAL CONSISTENCY:
 * - Services eventually consistent
 * - No immediate consistency requirement
 */
@Configuration
public class KafkaConfig {
    
    /**
     * KAFKA PRODUCER FACTORY
     * 
     * Creates Kafka producer for Payment Service
     * 
     * @return ProducerFactory - Kafka producer factory
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Kafka broker address
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        
        // Key serializer: String
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Value serializer: JSON (Payment objects serialized to JSON)
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    /**
     * KAFKA TEMPLATE BEAN
     * 
     * For publishing payment events
     * 
     * @return KafkaTemplate - Kafka template
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
