package com.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * OrderController handles HTTP requests for orders
 * 
 * Endpoints:
 * - POST /api/orders - Create new order
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Create a new order
     * 
     * Example request:
     * curl -X POST http://localhost:8080/api/orders \
     *   -H "Content-Type: application/json" \
     *   -d '{"productId": "PROD-123", "quantity": 2}'
     * 
     * Success response (200 OK):
     * {
     *   "id": "uuid-123",
     *   "productId": "PROD-123",
     *   "quantity": 2,
     *   "status": "CONFIRMED",
     *   "createdAt": "2026-07-18T..."
     * }
     * 
     * Error response (503 Service Unavailable):
     * {
     *   "error": "Inventory service unavailable after 3 attempts",
     *   "timestamp": "2026-07-18T..."
     * }
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        
        logger.info("📨 Received order request: {}", request);
        
        try {
            Order order = orderService.createOrder(request);
            logger.info("✅ Order successfully created: {}", order.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
            
        } catch (ServiceUnavailableException e) {
            logger.error("❌ Service unavailable: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            errorResponse.put("message", "Order processing failed. Please try again in a few moments.");
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            
        } catch (Exception e) {
            logger.error("❌ Unexpected error", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("details", e.getMessage());
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Order Service");
        response.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}
