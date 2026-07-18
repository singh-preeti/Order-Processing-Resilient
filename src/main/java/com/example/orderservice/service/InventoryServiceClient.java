package com.example.orderservice.service;

import com.example.orderservice.exception.ServiceUnavailableException;
import com.example.orderservice.exception.TimeoutException;
import com.example.orderservice.model.InventoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * InventoryServiceClient handles all communication with Inventory Service
 * 
 * Key feature: RETRY LOGIC
 * - If inventory service is slow/down, we try again
 * - Up to 3 attempts with delays between them
 * - Timeout: 5 seconds per request (don't wait forever)
 * 
 * Example usage:
 *   InventoryResponse resp = client.checkInventory("PROD-123");
 *   // Returns inventory or throws ServiceUnavailableException after 3 retries
 */
@Service
public class InventoryServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${inventory.service.url:http://localhost:8081}")
    private String inventoryServiceUrl;
    
    @Value("${inventory.service.retries:3}")
    private int maxRetries;
    
    @Value("${inventory.service.timeout:5000}")
    private long timeoutMs;
    
    /**
     * Check if product is available in inventory
     * Retries automatically if service is slow
     * 
     * Retry strategy:
     * Attempt 1 → Failed → Wait 1 second
     * Attempt 2 → Failed → Wait 2 seconds
     * Attempt 3 → Failed → Throw error
     * 
     * @param productId - Product to check
     * @return InventoryResponse with availability info
     * @throws ServiceUnavailableException - After 3 failed retries
     */
    public InventoryResponse checkInventory(String productId) {
        
        // We'll try up to 3 times
        int attemptNumber = 0;
        
        while (attemptNumber < maxRetries) {
            
            attemptNumber++;
            logger.info("🔄 Checking inventory for product: {} (Attempt {}/{})", 
                       productId, attemptNumber, maxRetries);
            
            try {
                // Call inventory service with a timeout
                InventoryResponse response = callInventoryServiceWithTimeout(productId);
                
                logger.info("✅ Inventory check succeeded for product: {}", productId);
                return response;
                
            } catch (TimeoutException e) {
                
                logger.warn("⏱️ Inventory service timeout on attempt {}", attemptNumber);
                
                // If this was the last attempt, give up
                if (attemptNumber >= maxRetries) {
                    logger.error("❌ Max retries ({}) reached for product: {}", 
                                maxRetries, productId);
                    throw new ServiceUnavailableException(
                        "Inventory service unavailable after " + maxRetries + " attempts"
                    );
                }
                
                // Wait before retrying
                long waitMs = getWaitTimeBeforeRetry(attemptNumber);
                logger.info("⏳ Waiting {} ms before retry...", waitMs);
                sleep(waitMs);
                
            } catch (RestClientException e) {
                
                logger.warn("❌ Inventory service error on attempt {}: {}", 
                           attemptNumber, e.getMessage());
                
                // If this was the last attempt, give up
                if (attemptNumber >= maxRetries) {
                    logger.error("❌ Max retries ({}) reached for product: {}", 
                                maxRetries, productId);
                    throw new ServiceUnavailableException(
                        "Inventory service error: " + e.getMessage(),
                        e
                    );
                }
                
                // Wait before retrying
                long waitMs = getWaitTimeBeforeRetry(attemptNumber);
                logger.info("⏳ Waiting {} ms before retry...", waitMs);
                sleep(waitMs);
            }
        }
        
        // This should never happen (thrown in catch block) but good to be explicit
        throw new ServiceUnavailableException(
            "Failed to check inventory after " + maxRetries + " attempts"
        );
    }
    
    /**
     * Call inventory service with a timeout
     * Throws TimeoutException if takes > 5 seconds
     */
    private InventoryResponse callInventoryServiceWithTimeout(String productId) {
        
        String url = inventoryServiceUrl + "/api/inventory/check?productId=" + productId;
        
        try {
            // Set timeout to 5 seconds
            RestTemplate timoutRestTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
            
            long startTime = System.currentTimeMillis();
            
            InventoryResponse response = timoutRestTemplate.getForObject(
                url,
                InventoryResponse.class
            );
            
            long elapsed = System.currentTimeMillis() - startTime;
            logger.debug("Inventory service response received in {} ms", elapsed);
            
            return response;
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Timeout or connection refused
            if (e.getRootCause() instanceof java.net.SocketTimeoutException) {
                throw new TimeoutException("Inventory service request timed out after " + 
                                          timeoutMs + "ms", e);
            }
            throw new RestClientException("Failed to call inventory service", e);
        }
    }
    
    /**
     * How long to wait before retrying
     * 
     * Attempt 1 → fails → wait 1 second
     * Attempt 2 → fails → wait 2 seconds
     * Attempt 3 → fails → give up
     */
    private long getWaitTimeBeforeRetry(int attemptNumber) {
        // After attempt 1: wait 1000ms
        // After attempt 2: wait 2000ms
        // After attempt 3: don't wait (we're done)
        return attemptNumber * 1000L;
    }
    
    /**
     * Helper to sleep without throwing checked exception
     */
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep was interrupted");
        }
    }
}
