package com.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * OrderService handles order creation and processing
 * 
 * Now with RESILIENCE:
 * - Uses InventoryServiceClient (which retries automatically)
 * - If inventory check fails after retries, returns error to user
 * - User knows immediately instead of waiting forever
 */
@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    // Inject the NEW InventoryServiceClient (instead of RestTemplate)
    @Autowired
    private InventoryServiceClient inventoryServiceClient;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Create a new order
     * 
     * Steps:
     * 1. Check if product is in stock (with retries)
     * 2. If in stock, create order
     * 3. Send confirmation notification
     * 4. Return order to user
     * 
     * @param request - Order request from user
     * @return Created order
     * @throws ServiceUnavailableException - If inventory service unavailable after retries
     */
    public Order createOrder(OrderRequest request) {
        
        logger.info("📦 Creating new order for product: {}, quantity: {}", 
                   request.getProductId(), request.getQuantity());
        
        try {
            // Call new client which automatically retries if inventory service is slow!
            InventoryResponse inventory = inventoryServiceClient.checkInventory(
                request.getProductId()
            );
            
            logger.info("✅ Inventory check passed. Available: {}", inventory.getAvailable());
            
            // Create the order
            Order order = new Order();
            order.setProductId(request.getProductId());
            order.setQuantity(request.getQuantity());
            order.setStatus("CONFIRMED");
            order.setCreatedAt(Instant.now().toString());
            
            logger.info("✅ Order created: {}", order.getId());
            
            // Try to send notification (but don't fail if it's slow)
            try {
                notificationService.sendOrderConfirmation(order);
            } catch (Exception e) {
                logger.warn("⚠️ Failed to send notification, but order was created: {}", 
                           e.getMessage());
                // Continue - don't fail the order just because notification is slow
            }
            
            return order;
            
        } catch (ServiceUnavailableException e) {
            // Inventory service failed after retries
            logger.error("❌ Cannot create order - inventory service unavailable: {}", 
                        e.getMessage());
            
            throw e; // Let client know order couldn't be created
        }
    }
}
