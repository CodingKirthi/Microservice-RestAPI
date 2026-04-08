package com.microservices.orderservice.service;

import com.microservices.orderservice.client.ProductClient;
import com.microservices.orderservice.client.ProductResponse;
import com.microservices.orderservice.dto.CreateOrderRequest;
import com.microservices.orderservice.dto.OrderDTO;
import com.microservices.orderservice.model.Order;
import com.microservices.orderservice.model.OrderStatus;
import com.microservices.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;
    private ProductResponse availableProduct;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order(1L, "Laptop", 2, new BigDecimal("2000.00"), OrderStatus.CONFIRMED);
        sampleOrder.setId(1L);

        availableProduct = new ProductResponse();
        availableProduct.setProductName("Laptop");
        availableProduct.setAvailable(true);
        availableProduct.setPrice(new BigDecimal("1000.00"));
    }

    @Test
    void getAllOrders_returnsAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder));

        List<OrderDTO> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getProductName());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_existingId_returnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        OrderDTO result = orderService.getOrderById(1L);

        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void getOrderById_nonExistingId_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.getOrderById(99L));

        assertTrue(exception.getMessage().contains("Order not found with id: 99"));
    }

    @Test
    void getOrdersByStatus_returnsFilteredOrders() {
        when(orderRepository.findByStatus(OrderStatus.CONFIRMED)).thenReturn(List.of(sampleOrder));

        List<OrderDTO> result = orderService.getOrdersByStatus("CONFIRMED");

        assertEquals(1, result.size());
        assertEquals(OrderStatus.CONFIRMED, result.get(0).getStatus());
    }

    @Test
    void createOrder_productAvailable_returnsConfirmedOrder() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 2);
        when(productClient.checkStock(anyLong(), anyInt())).thenReturn(availableProduct);
        doNothing().when(productClient).reduceStock(anyLong(), anyInt());
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderDTO result = orderService.createOrder(request);

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals("Laptop", result.getProductName());
        verify(productClient, times(1)).checkStock(1L, 2);
        verify(productClient, times(1)).reduceStock(1L, 2);
    }

    @Test
    void createOrder_productUnavailable_returnsFailedOrder() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 2);
        ProductResponse unavailable = new ProductResponse();
        unavailable.setProductName("Laptop");
        unavailable.setAvailable(false);
        unavailable.setPrice(BigDecimal.ZERO);

        Order failedOrder = new Order(1L, "Laptop", 2, BigDecimal.ZERO, OrderStatus.FAILED);
        failedOrder.setId(2L);

        when(productClient.checkStock(anyLong(), anyInt())).thenReturn(unavailable);
        when(orderRepository.save(any(Order.class))).thenReturn(failedOrder);

        OrderDTO result = orderService.createOrder(request);

        assertEquals(OrderStatus.FAILED, result.getStatus());
        verify(productClient, never()).reduceStock(anyLong(), anyInt());
    }

    @Test
    void createOrderFallback_savesOrderWithFallbackStatus() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 2);
        Order fallbackOrder = new Order(1L, "UNAVAILABLE (Product Service down)", 2, BigDecimal.ZERO, OrderStatus.FALLBACK);
        fallbackOrder.setId(3L);

        when(orderRepository.save(any(Order.class))).thenReturn(fallbackOrder);

        OrderDTO result = orderService.createOrderFallback(request, new RuntimeException("Connection refused"));

        assertEquals(OrderStatus.FALLBACK, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
