package com.order;

/**
 * OrderRequest is what user sends when placing an order
 * 
 * Example JSON from user:
 * {
 *   "productId": "PROD-123",
 *   "quantity": 5
 * }
 */
public class OrderRequest {
    private String productId;
    private int quantity;
    
    public OrderRequest() {}
    
    public OrderRequest(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    @Override
    public String toString() {
        return "OrderRequest{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
