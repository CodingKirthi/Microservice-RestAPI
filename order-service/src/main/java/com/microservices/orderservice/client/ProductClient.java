package com.microservices.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for communicating with Product Service via Eureka service discovery.
 * The name "PRODUCT-SERVICE" is the Eureka service ID — no hardcoded URL is used.
 */
@FeignClient(name = "PRODUCT-SERVICE", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/products/{id}/check-stock")
    ProductResponse checkStock(@PathVariable("id") Long productId,
                               @RequestParam("quantity") int quantity);

    @PostMapping("/api/products/{id}/reduce-stock")
    void reduceStock(@PathVariable("id") Long productId,
                     @RequestParam("quantity") int quantity);
}
