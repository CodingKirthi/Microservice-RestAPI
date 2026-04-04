package com.microservices.orderservice.service;

import com.microservices.orderservice.client.ProductClient;
import com.microservices.orderservice.client.ProductResponse;
import com.microservices.orderservice.dto.CreateOrderRequest;
import com.microservices.orderservice.dto.OrderDTO;
import com.microservices.orderservice.model.Order;
import com.microservices.orderservice.model.OrderStatus;
import com.microservices.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String PRODUCT_SERVICE = "product-service";

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    /**
     * Creates an order by calling Product Service (Service B) via Feign + Eureka.
     * Circuit breaker and retry are applied. If Product Service is down,
     * the fallback saves the order with FALLBACK status.
     */
    @CircuitBreaker(name = PRODUCT_SERVICE, fallbackMethod = "createOrderFallback")
    @Retry(name = PRODUCT_SERVICE)
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("Creating order for productId={}, quantity={}", request.getProductId(), request.getQuantity());

        // Call Product Service via Feign (resolved by Eureka, not hardcoded URL)
        ProductResponse stockInfo = productClient.checkStock(request.getProductId(), request.getQuantity());
        log.info("Stock check result: product='{}', available={}, price={}",
                stockInfo.getProductName(), stockInfo.isAvailable(), stockInfo.getPrice());

        if (!stockInfo.isAvailable()) {
            Order order = new Order(
                    request.getProductId(),
                    stockInfo.getProductName(),
                    request.getQuantity(),
                    BigDecimal.ZERO,
                    OrderStatus.FAILED
            );
            order.setFailureReason("Insufficient stock");
            return toDTO(orderRepository.save(order));
        }

        BigDecimal totalPrice = stockInfo.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        // Reduce stock in Product Service
        productClient.reduceStock(request.getProductId(), request.getQuantity());

        Order order = new Order(
                request.getProductId(),
                stockInfo.getProductName(),
                request.getQuantity(),
                totalPrice,
                OrderStatus.CONFIRMED
        );
        Order saved = orderRepository.save(order);
        log.info("Order {} confirmed for product '{}', total={}", saved.getId(), saved.getProductName(), totalPrice);
        return toDTO(saved);
    }

    /**
     * Fallback method: called when circuit is open or retries exhausted.
     * The order is saved with FALLBACK status so the user gets a response
     * instead of an error — demonstrating graceful degradation.
     */
    public OrderDTO createOrderFallback(CreateOrderRequest request, Throwable t) {
        log.error("CIRCUIT BREAKER FALLBACK triggered for productId={}. Reason: {}",
                request.getProductId(), t.getMessage());

        Order order = new Order(
                request.getProductId(),
                "UNAVAILABLE (Product Service down)",
                request.getQuantity(),
                BigDecimal.ZERO,
                OrderStatus.FALLBACK
        );
        order.setFailureReason("Product Service unavailable: " + t.getMessage());
        Order saved = orderRepository.save(order);
        log.warn("Fallback order {} saved with status FALLBACK", saved.getId());
        return toDTO(saved);
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return toDTO(order);
    }

    public List<OrderDTO> getOrdersByStatus(String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setProductId(order.getProductId());
        dto.setProductName(order.getProductName());
        dto.setQuantity(order.getQuantity());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setFailureReason(order.getFailureReason());
        return dto;
    }
}
