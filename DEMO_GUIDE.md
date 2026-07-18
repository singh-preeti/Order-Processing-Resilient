# 📚 Demo Guide - How to Present This Project

## 🎯 Objective
Demonstrate how to make microservices resilient to failures using retry logic and timeout handling.

## ⏱️ Demo Duration: 10 minutes

---

## PART 1: Problem Introduction (2 minutes)

### Show This Scenario
```
Scenario: Black Friday Traffic Spike 🛍️

Normal Day:
  User places order
    ↓
  Order Service calls Inventory Service (1-2 seconds)
    ↓
  Inventory responds with stock info ✅
    ↓
  Order created

Black Friday:
  Millions of users place orders
    ↓
  Inventory Service gets overwhelmed
    ↓
  Order Service waits 30 seconds (timeout!)
    ↓
  User refresh page (angry 😤)
    ↓
  Order gets placed twice (duplicate charge!)
    ↓
  Revenue loss: $50,000+ ❌
```

### The Problem
> "Without retry logic, a temporary delay in Inventory Service causes entire order processing to fail"

---

## PART 2: Solution Architecture (3 minutes)

### Before vs After

**BEFORE (No Retry):**
```java
// OrderService.java
public Order createOrder(OrderRequest request) {
    // Direct call - fails if slow
    inventory = restTemplate.getForObject(url);
    // ❌ If this times out, entire order fails
}
```

**AFTER (With Retry):**
```java
// OrderService.java
public Order createOrder(OrderRequest request) {
    // Uses client with retry logic
    inventory = inventoryServiceClient.checkInventory(productId);
    // ✅ Client automatically retries 3 times!
}

// InventoryServiceClient.java
public InventoryResponse checkInventory(String productId) {
    int attemptNumber = 0;
    
    while (attemptNumber < maxRetries) {
        attemptNumber++;
        try {
            return callInventoryServiceWithTimeout(productId);
        } catch (TimeoutException e) {
            if (attemptNumber < maxRetries) {
                sleep(getWaitTimeBeforeRetry(attemptNumber));
            }
        }
    }
    throw new ServiceUnavailableException(...);
}
```

### Retry Strategy Diagram
```
Attempt 1
   └─ Call Inventory Service
      └─ Timeout? ❌ Wait 1 second

Attempt 2
   └─ Call Inventory Service
      └─ Timeout? ❌ Wait 2 seconds

Attempt 3
   └─ Call Inventory Service
      └─ Success! ✅ Return data
      
Total time: ~3 seconds (instead of 15+ seconds of hanging)
```

---

## PART 3: Live Demo (5 minutes)

### Step 1: Show the Code Structure
```bash
tree -I 'target' src/
```

Point out:
- `InventoryServiceClient.java` - The retry logic ⭐
- `OrderService.java` - Uses the client
- `OrderServiceTest.java` - Tests for it

### Step 2: Run the Tests
```bash
mvn test
```

**Expected Output:**
```
Running com.example.orderservice.service.OrderServiceTest

✅ Test 1 PASSED: Order created successfully
✅ Test 2 PASSED: Error handled gracefully
✅ Test 3 PASSED: Inventory service called once
✅ Test 4 PASSED: All order details verified

[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

**Highlight:**
> "All tests passing means our retry logic works correctly in different scenarios"

### Step 3: Show Configuration
```bash
cat src/main/resources/application.properties
```

Explain:
```properties
inventory.service.retries=3          # Try 3 times
inventory.service.timeout=5000       # Max 5 seconds per attempt
```

### Step 4: Start the Application
```bash
mvn spring-boot:run
```

**Point out in logs:**
```
2026-07-18 12:34:56 [INFO] ... Order Service started on port 8080
2026-07-18 12:34:56 [INFO] ... Ready to process orders
```

### Step 5: Test Creating an Order

**In another terminal:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-123", "quantity": 2}'
```

**Expected Error (because no inventory service):**
```json
{
  "error": "Inventory service unavailable after 3 attempts",
  "timestamp": "2026-07-18T12:34:56Z",
  "message": "Order processing failed. Please try again in a few moments."
}
```

**Show logs in application terminal:**
```
🔄 Checking inventory (Attempt 1/3)
⏱️ Timeout on attempt 1, waiting 1000ms...
🔄 Checking inventory (Attempt 2/3)
⏱️ Timeout on attempt 2, waiting 2000ms...
🔄 Checking inventory (Attempt 3/3)
❌ Max retries reached
```

**Explain:**
> "See how it tried 3 times automatically? Each time waiting longer before retrying."

### Step 6: Show Key Code

**Open InventoryServiceClient.java and show:**

```java
private long getWaitTimeBeforeRetry(int attemptNumber) {
    // After attempt 1: wait 1000ms
    // After attempt 2: wait 2000ms
    return attemptNumber * 1000L;
}
```

**Explain:**
> "This exponential backoff prevents hammering the service when it's overwhelmed"

---

## PART 4: Key Takeaways (Interactive)

Ask the audience:

1. **What problem did we solve?**
   > Answer: Temporary service unavailability no longer breaks order processing

2. **How many times do we retry?**
   > Answer: 3 times (configurable in application.properties)

3. **What happens if all retries fail?**
   > Answer: We throw ServiceUnavailableException with a clear message

4. **Why use exponential backoff?**
   > Answer: Gives service time to recover, prevents overwhelming it further

5. **Where would you add circuit breaker next?**
   > Answer: In InventoryServiceClient, after N failures, stop trying temporarily

---

## 📊 Comparison Slides

### Before (No Resilience)
| Scenario | Result | Time |
|----------|--------|------|
| Inventory fast | ✅ Order created | 1s |
| Inventory slow (5s) | ❌ Timeout error | 5s |
| Inventory down | ❌ Connection error | 30s |

### After (With Resilience)
| Scenario | Result | Time |
|----------|--------|------|
| Inventory fast | ✅ Order created | 1s |
| Inventory slow (5s) | ✅ Order created (after retry) | ~6s |
| Inventory down | ❌ Clear error | ~3s (3 retries) |

**Key Metric:**
> "In failure scenarios, we now fail fast (3 seconds) instead of hanging (30 seconds)"

---

## 🔧 Demo Notes

### Things to Emphasize
✅ **Logging**: Show how each retry is logged  
✅ **Configuration**: Show how easy it is to change retry count/timeout  
✅ **Testing**: Show that different scenarios are covered by tests  
✅ **User Experience**: User gets error in ~3s instead of waiting 30s  
✅ **Gradual Recovery**: Retries allow service to recover during delays  

### Common Questions

**Q: Why not retry forever?**
A: Because users need feedback. After 3 attempts (~3 seconds), they should know something is wrong and try again later.

**Q: What if network is truly down?**
A: After 3 failed retries, we tell the user clearly: "Service unavailable, please try again in a few moments."

**Q: Can we use this for all services?**
A: Not for everything. Use retry for:
- ✅ Temporary failures (timeouts, 5xx errors)
- ❌ Not for permanent failures (404 Not Found, 401 Unauthorized)

---

## 📈 Expected Time Breakdown

- **Introduction**: 2 min
- **Architecture explanation**: 3 min
- **Code walkthrough**: 3 min
- **Live demo + Q&A**: 2 min

**Total: ~10 minutes** ✅

---

## 🎓 What Audience Learns

After this demo, audience will understand:

1. **The Problem**: Why resilience patterns matter
2. **The Solution**: How retry logic with timeouts works
3. **The Implementation**: Spring Boot service with retry client
4. **The Testing**: How to test different failure scenarios
5. **The Configuration**: How to adjust retry behavior without code changes
6. **The Patterns**: Exponential backoff and timeout patterns in practice

---

## 💡 Optional Extensions

If time allows, you can show:

1. **Different retry scenarios**:
   - Change timeout to 1 second to see more retries
   - Change max retries to 5 to show longer wait times

2. **Monitoring metrics**:
   - Show how many retry attempts per product
   - Track success rate over time

3. **Circuit breaker pattern**:
   - Explain how to add it on top of retry logic
   - Prevent retries when service is completely down

---

**Good luck with your demo! 🚀**
