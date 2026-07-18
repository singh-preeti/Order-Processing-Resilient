package com.order.model;

import java.util.UUID;

/**
 * Order represents a customer's order
 * 
 * Example:
 * Order order = new Order();
 * order.setId("123");
 * order.setProductId("PROD-456");
 * order.setQuantity(2);
 */
public class Order {
    
    private String id;
    private String productId;
    private int quantity;
    private String status; // "CREATED", "CONFIRMED", "FAILED"
    private String createdAt;
    
    public Order() {
        this.id = UUID.randomUUID().toString();
    }
    
    public Order(String productId, int quantity) {
        this();
        this.productId = productId;
        this.quantity = quantity;
        this.status = "CREATED";
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
