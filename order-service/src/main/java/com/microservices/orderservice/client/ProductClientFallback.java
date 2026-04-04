package com.microservices.orderservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fallback implementation for ProductClient.
 * Called automatically when Product Service is unavailable (circuit open).
 */
@Component
public class ProductClientFallback implements ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallback.class);

    @Override
    public ProductResponse checkStock(Long productId, int quantity) {
        log.warn("FALLBACK: Product Service unavailable. Returning fallback for productId={}", productId);
        ProductResponse fallback = new ProductResponse();
        fallback.setProductId(productId);
        fallback.setProductName("UNKNOWN (service unavailable)");
        fallback.setPrice(BigDecimal.ZERO);
        fallback.setAvailable(false);
        fallback.setRequestedQuantity(quantity);
        return fallback;
    }

    @Override
    public void reduceStock(Long productId, int quantity) {
        log.warn("FALLBACK: Product Service unavailable. Stock reduction skipped for productId={}", productId);
    }
}
