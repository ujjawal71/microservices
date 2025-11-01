package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.ProductDto;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public OrderService(OrderRepository orderRepository, ProductClient productClient,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Transactional
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    public Order createOrder(OrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(Order.OrderStatus.PENDING);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        List<OrderItem> items = new ArrayList<>();
        for (var itemDto : request.getItems()) {
            try {
                System.out.println("Fetching product: " + itemDto.getProductId());
                ProductDto product = productClient.getProduct(itemDto.getProductId());
                System.out.println("Product fetched: " + product.getId() + " - " + product.getName());
                
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(product.getPrice());
                
                items.add(orderItem);
            } catch (Exception e) {
                System.err.println("Failed to fetch product " + itemDto.getProductId() + ": " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to fetch product information: " + e.getMessage(), e);
            }
        }
        
        totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);
        
        // Publish order event to Kafka (completely async, non-blocking)
        // Kafka is optional - if it fails, don't block order creation
        // Run in separate thread to ensure it never blocks
        new Thread(() -> {
            try {
                kafkaTemplate.send("order-created", savedOrder);
                System.out.println("Kafka event published (async)");
            } catch (Exception e) {
                // Log but don't fail - Kafka is optional
                System.err.println("Kafka publish failed (non-critical): " + e.getMessage());
            }
        }).start();
        
        return savedOrder;
    }
    
    public Order createOrderFallback(OrderRequest request, Exception ex) {
        // Fallback when order creation fails
        throw new RuntimeException("Order service temporarily unavailable. Please try again later.", ex);
    }
    
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
