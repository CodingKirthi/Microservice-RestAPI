package com.microservices.productservice;

import com.microservices.productservice.model.Product;
import com.microservices.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final ProductRepository productRepository;

    public DataLoader(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Loading sample product data...");
        productRepository.save(new Product("Laptop Pro 15",    "High-performance laptop",  new BigDecimal("1299.99"), 50));
        productRepository.save(new Product("Wireless Mouse",   "Ergonomic wireless mouse",  new BigDecimal("29.99"),  200));
        productRepository.save(new Product("USB-C Hub",        "7-in-1 USB-C hub",          new BigDecimal("49.99"),  150));
        productRepository.save(new Product("Mechanical Keyboard","RGB mechanical keyboard", new BigDecimal("89.99"),  75));
        productRepository.save(new Product("4K Monitor",       "27-inch 4K display",        new BigDecimal("499.99"), 30));
        log.info("Sample data loaded: {} products", productRepository.count());
    }
}
