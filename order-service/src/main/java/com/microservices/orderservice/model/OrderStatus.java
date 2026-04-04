package com.microservices.orderservice.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    FALLBACK  // Used when product-service is unavailable
}
