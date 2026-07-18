package com.example.orderservice.model;

/**
 * InventoryResponse is what Inventory Service returns
 * 
 * Example:
 * {
 *   "productId": "PROD-123",
 *   "available": 50,
 *   "reserved": 10
 * }
 */
public class InventoryResponse {
    private String productId;
    private int available;
    private int reserved;
    
    public InventoryResponse() {}
    
    public InventoryResponse(String productId, int available, int reserved) {
        this.productId = productId;
        this.available = available;
        this.reserved = reserved;
    }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    
    public int getReserved() { return reserved; }
    public void setReserved(int reserved) { this.reserved = reserved; }
    
    @Override
    public String toString() {
        return "InventoryResponse{" +
                "productId='" + productId + '\'' +
                ", available=" + available +
                ", reserved=" + reserved +
                '}';
    }
}
