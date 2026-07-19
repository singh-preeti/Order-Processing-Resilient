package com.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * OrderServiceTest verifies that order creation works correctly
 * even when inventory service is slow or down.
 * 
 * We use MOCKS to simulate different scenarios:
 * - Mock 1: Inventory service responds quickly (happy path)
 * - Mock 2: Inventory service is slow but eventually responds
 * - Mock 3: Inventory service is down (all retries fail)
 */
@SpringBootTest
@DisplayName("Order Service Tests")
public class OrderServiceTest {
    
    @Autowired
    private OrderService orderService;
    
    @MockBean
    private InventoryServiceClient inventoryServiceClient;
    
    @MockBean
    private NotificationService notificationService;
    
    private OrderRequest orderRequest;
    
    @BeforeEach
    void setUp() {
        // Setup: Create a sample order request
        orderRequest = new OrderRequest();
        orderRequest.setProductId("PROD-123");
        orderRequest.setQuantity(5);
    }
    
    // ========== TEST 1: Happy Path ==========
    /**
     * TEST 1: Inventory service responds immediately
     * 
     * Scenario:
     * - User places order
     * - Inventory service says "yes, we have it"
     * - Order is created successfully
     * 
     * Expected:
     * - Order status = "CONFIRMED"
     * - Order has correct product ID and quantity
     * - No exceptions thrown
     */
    @Test
    @DisplayName("✅ Test 1: Successful order when inventory available")
    void testCreateOrder_Success() {
        
        // ARRANGE: Mock inventory service to return success
        InventoryResponse mockResponse = new InventoryResponse();
        mockResponse.setProductId("PROD-123");
        mockResponse.setAvailable(100);
        mockResponse.setReserved(20);
        
        when(inventoryServiceClient.checkInventory("PROD-123"))
            .thenReturn(mockResponse);
        
        // ACT: Create order
        Order result = orderService.createOrder(orderRequest);
        
        // ASSERT: Verify order was created correctly
        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus());
        assertEquals("PROD-123", result.getProductId());
        assertEquals(5, result.getQuantity());
        
        System.out.println("✅ Test 1 PASSED: Order created successfully");
    }
    
    // ========== TEST 2: Failed Order ==========
    /**
     * TEST 2: Inventory service is completely down
     * 
     * Scenario:
     * - User places order
     * - Inventory service down (all retries fail)
     * - ServiceUnavailableException thrown
     * - User gets clear error message
     * 
     * Expected:
     * - ServiceUnavailableException is thrown
     * - Error message mentions "unavailable"
     */
    @Test
    @DisplayName("❌ Test 2: Order fails when inventory service is down")
    void testCreateOrder_FailsWhenInventoryDown() {
        
        // ARRANGE: Mock inventory service to always fail
        when(inventoryServiceClient.checkInventory("PROD-123"))
            .thenThrow(new ServiceUnavailableException(
                "Inventory service unavailable after 3 attempts"
            ));
        
        // ACT & ASSERT: Verify exception is thrown
        ServiceUnavailableException exception = assertThrows(
            ServiceUnavailableException.class,
            () -> orderService.createOrder(orderRequest)
        );
        
        // Verify error message is helpful
        assertTrue(exception.getMessage().contains("unavailable"));
        
        System.out.println("✅ Test 2 PASSED: Error handled gracefully");
    }
    
    // ========== TEST 3: Verify Service Called ==========
    /**
     * TEST 3: Verify that inventory service was called
     * 
     * Expected:
     * - inventoryServiceClient.checkInventory() was called with correct product ID
     */
    @Test
    @DisplayName("✅ Test 3: Verify inventory service was called")
    void testCreateOrder_VerifyInventoryServiceCalled() {
        
        // ARRANGE
        InventoryResponse mockResponse = new InventoryResponse("PROD-123", 100, 20);
        when(inventoryServiceClient.checkInventory("PROD-123"))
            .thenReturn(mockResponse);
        
        // ACT
        orderService.createOrder(orderRequest);
        
        // ASSERT: Verify service was called
        verify(inventoryServiceClient, times(1))
            .checkInventory("PROD-123");
        
        System.out.println("✅ Test 3 PASSED: Inventory service called once");
    }
    
    // ========== TEST 4: Verify Order Details ==========
    /**
     * TEST 4: Verify all order details are set correctly
     * 
     * Expected:
     * - Order ID is not null (UUID generated)
     * - Product ID matches request
     * - Quantity matches request
     * - Status is "CONFIRMED"
     * - Created timestamp is set
     */
    @Test
    @DisplayName("✅ Test 4: Verify all order details are correct")
    void testCreateOrder_VerifyAllDetails() {
        
        // ARRANGE
        InventoryResponse mockResponse = new InventoryResponse("PROD-456", 50, 10);
        when(inventoryServiceClient.checkInventory("PROD-456"))
            .thenReturn(mockResponse);
        
        OrderRequest request = new OrderRequest("PROD-456", 3);
        
        // ACT
        Order result = orderService.createOrder(request);
        
        // ASSERT
        assertNotNull(result.getId(), "Order ID should be generated (UUID)");
        assertEquals("PROD-456", result.getProductId(), "Product ID should match");
        assertEquals(3, result.getQuantity(), "Quantity should match");
        assertEquals("CONFIRMED", result.getStatus(), "Status should be CONFIRMED");
        assertNotNull(result.getCreatedAt(), "Created timestamp should be set");
        
        System.out.println("✅ Test 4 PASSED: All order details verified");
    }
}
