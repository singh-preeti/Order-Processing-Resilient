package com.example.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Order Service Application - Main entry point
 * 
 * This is a microservice that processes customer orders with resilience.
 * Features:
 * - Automatic retry logic when inventory service is slow
 * - Timeout handling (don't wait forever)
 * - Detailed logging for debugging
 * - Comprehensive error handling
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
