package com.ecommerce.order.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * KAFKA CONFIGURATION - Event-Driven Architecture Setup
 * 
 * ========================================================================
 * KAFKA CONCEPTS
 * ========================================================================
 * 
 * 1. EVENT-DRIVEN ARCHITECTURE:
 *    - Services communicate through events (not direct calls)
 *    - Asynchronous messaging
 *    - Loose coupling between services
 * 
 * 2. PUBLISH-SUBSCRIBE PATTERN:
 *    - Producer publishes events to topics
 *    - Consumers subscribe to topics
 *    - Multiple consumers can listen to same topic
 * 
 * 3. MESSAGE BROKER:
 *    - Kafka acts as message broker
 *    - Stores messages persistently
 *    - Handles message delivery
 * 
 * ========================================================================
 * EVENT-DRIVEN BENEFITS
 * ========================================================================
 * 
 * 1. LOOSE COUPLING:
 *    - Services don't know about each other
 *    - Order Service publishes event → doesn't know who listens
 * 
 * 2. SCALABILITY:
 *    - Multiple consumers can process events in parallel
 *    - Horizontal scaling
 * 
 * 3. RESILIENCE:
 *    - If consumer down → events queued
 *    - Consumer can catch up later
 * 
 * 4. EVENTUAL CONSISTENCY:
 *    - Services eventually consistent
 *    - No immediate consistency requirement
 * 
 * ========================================================================
 * KAFKA PRODUCER CONFIGURATION
 * ========================================================================
 * 
 * PRODUCER:
 * - Publishes events to Kafka topics
 * - Serializes messages (JSON)
 * - Handles message delivery
 */
@Configuration // Spring configuration class
public class KafkaConfig {
    
    /**
     * KAFKA PRODUCER FACTORY
     * 
     * Creates Kafka producer instances
     * 
     * CONFIGURATION:
     * - Bootstrap servers: Kafka broker address
     * - Key serializer: String (for topic partitioning)
     * - Value serializer: JSON (for complex objects)
     * 
     * KAFKA TOPICS USED:
     * - "order-created": Published when order is created
     *   Consumers: Inventory Service, Notification Service
     * 
     * @return ProducerFactory - Kafka producer factory
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Kafka broker address (bootstrap servers)
        // Kafka runs on localhost:9092
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        
        // Key serializer: String (used for topic partitioning)
        // Partition key determines which partition message goes to
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Value serializer: JSON (serializes Java objects to JSON)
        // Order objects serialized to JSON for transmission
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    /**
     * KAFKA TEMPLATE BEAN
     * 
     * Spring abstraction for Kafka producer
     * 
     * USAGE:
     * - kafkaTemplate.send("order-created", orderObject)
     * - Publishes event to Kafka topic
     * 
     * ASYNCHRONOUS:
     * - Non-blocking operation
     * - Doesn't wait for consumer to process
     * - Fire-and-forget pattern
     * 
     * @return KafkaTemplate - Kafka template for sending messages
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
