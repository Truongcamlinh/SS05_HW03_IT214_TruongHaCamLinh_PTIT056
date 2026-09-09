package com.vietmart.productservice;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/products")
class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Value("${server.port}")
    private int port;

    @GetMapping
    List<Product> findAll() {
        log.info("product-service instance port {} handled GET /api/products", port);
        return List.of(
                new Product(1L, "Laptop VietMart", 15500000),
                new Product(2L, "Keyboard VietMart", 450000),
                new Product(3L, "Mouse VietMart", 220000));
    }

    @GetMapping("/instance")
    InstanceResponse instance() {
        log.info("product-service instance port {} handled GET /api/products/instance", port);
        return new InstanceResponse("product-service", port);
    }
}

record Product(Long id, String name, long price) {
}

record InstanceResponse(String service, int port) {
}
