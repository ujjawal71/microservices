package com.ecommerce.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", url = "")
public interface OrderClient {
    @PutMapping("/api/orders/{id}/status")
    Object updateOrderStatus(@PathVariable Long id, @RequestParam String status);
}

