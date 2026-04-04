package com.microservices.productservice.controller;

import com.microservices.productservice.dto.ProductDTO;
import com.microservices.productservice.model.Product;
import com.microservices.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Value("${product.service.message:Product Service Running}")
    private String serviceMessage;

    @Value("${server.port:8081}")
    private String serverPort;

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("GET /api/products - instance on port {}", serverPort);
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - instance on port {}", id, serverPort);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody Product product) {
        log.info("POST /api/products - creating product: {}", product.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,
                                                     @Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Called internally by Order Service via service discovery
    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Map<String, Object>> checkStock(@PathVariable Long id,
                                                           @RequestParam int quantity) {
        boolean available = productService.checkStock(id, quantity);
        ProductDTO product = productService.getProductById(id);
        log.info("Stock check for product {} (qty {}): available={}", id, quantity, available);
        return ResponseEntity.ok(Map.of(
                "productId", id,
                "productName", product.getName(),
                "price", product.getPrice(),
                "available", available,
                "requestedQuantity", quantity
        ));
    }

    @PostMapping("/{id}/reduce-stock")
    public ResponseEntity<Void> reduceStock(@PathVariable Long id, @RequestParam int quantity) {
        productService.reduceStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
                "service", "product-service",
                "port", serverPort,
                "message", serviceMessage
        ));
    }
}
