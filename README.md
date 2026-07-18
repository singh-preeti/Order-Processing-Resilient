# Order Processing System with Resilience

🎯 **A production-grade microservice demonstrating retry logic, timeout handling, and resilience patterns**

## 📋 What This Project Does

This is a **simplified but complete Java microservice** that:
- Processes customer orders
- Automatically retries if the Inventory Service is slow
- Handles timeouts gracefully
- Provides detailed logging for debugging
- Includes comprehensive unit tests

## ⚡ Key Features

### 1. **Automatic Retry Logic** 🔄
When the Inventory Service is slow:
```
Attempt 1: Call inventory service
  └─ Failed (timeout)
Wait 1 second ⏳

Attempt 2: Call inventory service
  └─ Failed (timeout)
Wait 2 seconds ⏳

Attempt 3: Call inventory service
  └─ Success! ✅
```

### 2. **Timeout Handling** ⏱️
- Never wait more than 5 seconds for inventory service
- If it takes longer, try again
- After 3 attempts, return clear error to user

### 3. **Detailed Logging** 📝
Each attempt is logged so you can debug what happened:
```
🔄 Checking inventory (Attempt 1/3)
⏱️ Timeout on attempt 1, waiting 1000ms...
🔄 Checking inventory (Attempt 2/3)
✅ Inventory check succeeded
```

### 4. **Comprehensive Tests** ✅
4 test cases covering:
- Successful order creation
- Failed order (inventory down)
- Service call verification
- Order detail validation

## 🏗️ Project Structure

```
src/
├── main/java/com/example/orderservice/
│   ├── controller/
│   │   └── OrderController.java          (HTTP endpoints)
│   ├── service/
│   │   ├── OrderService.java             (Business logic)
│   │   ├── InventoryServiceClient.java   (RETRY LOGIC HERE)
│   │   └── NotificationService.java
│   ├── model/
│   │   ├── Order.java
│   │   ├── OrderRequest.java
│   │   └── InventoryResponse.java
│   ├── exception/
│   │   ├── ServiceUnavailableException.java
│   │   └── TimeoutException.java
│   └── OrderServiceApplication.java
├── test/java/com/example/orderservice/
│   └── service/
│       └── OrderServiceTest.java         (Unit tests)
├── resources/
│   └── application.properties            (Configuration)
└── pom.xml                               (Dependencies)
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Git

### 1. Clone and Navigate
```bash
git clone https://github.com/singh-preeti/Order-Processing-Resilient.git
cd Order-Processing-Resilient
```

### 2. Build the Project
```bash
mvn clean install
```

### 3. Run Tests
```bash
mvn test
```

Expected output:
```
✅ Test 1 PASSED: Order created successfully
✅ Test 2 PASSED: Error handled gracefully
✅ Test 3 PASSED: Inventory service called once
✅ Test 4 PASSED: All order details verified

[INFO] Tests run: 4, Failures: 0, Errors: 0
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

You should see:
```
  .   ____          _            __ _ _
 /\\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \\
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \\
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Starting Order Service v1.0.0...
Listening on http://localhost:8080
```

## 🔌 How to Test

### Test 1: Create a Successful Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-123", "quantity": 5}'
```

**Expected response (first time will fail because no Inventory Service):**
```json
{
  "error": "Inventory service unavailable after 3 attempts",
  "timestamp": "2026-07-18T...",
  "message": "Order processing failed. Please try again in a few moments."
}
```

### Test 2: Start Mock Inventory Service
In another terminal, create a mock Inventory Service:

```bash
# Use Python to mock the service
python3 -m http.server 8081
```

Or create a simple Node.js server:
```bash
npm install -g http-server
http-server -p 8081
```

Or use Java:
```java
// Simple mock server on port 8081
// Returns: {"productId": "PROD-123", "available": 100, "reserved": 20}
```

### Test 3: Check Health
```bash
curl http://localhost:8080/api/orders/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "Order Service",
  "timestamp": "2026-07-18T12:34:56Z"
}
```

## 📊 Configuration

Edit `src/main/resources/application.properties` to customize:

```properties
# Inventory service URL
inventory.service.url=http://localhost:8081

# Number of retries (1-5 recommended)
inventory.service.retries=3

# Timeout per request in milliseconds
inventory.service.timeout=5000

# Logging level (ERROR, WARN, INFO, DEBUG)
logging.level.com.example.orderservice=INFO
```

## 🔍 Understanding the Retry Logic

### InventoryServiceClient.java - The Core

This is the main class that implements retry logic:

```java
public InventoryResponse checkInventory(String productId) {
    int attemptNumber = 0;
    
    while (attemptNumber < maxRetries) {  // Try up to 3 times
        attemptNumber++;
        
        try {
            // Try to call inventory service
            return callInventoryServiceWithTimeout(productId);
            
        } catch (TimeoutException e) {
            // Failed, but not the last attempt
            if (attemptNumber < maxRetries) {
                long waitMs = getWaitTimeBeforeRetry(attemptNumber);
                sleep(waitMs);  // Wait before retrying
            }
        }
    }
    
    // All retries failed
    throw new ServiceUnavailableException(...);
}
```

**Key points:**
1. **Line 2-3**: Loop up to 3 times
2. **Line 8**: Try to get inventory
3. **Line 10-14**: If timeout, wait and try again
4. **Line 17**: If all retries fail, throw error

## 📈 What Happens in Each Scenario

### Scenario 1: Inventory Service is Fast ✅
```
Attempt 1 → Success immediately
Total time: ~100ms
Result: Order created
```

### Scenario 2: Inventory Service is Slow (5-10 seconds) ⏳
```
Attempt 1 → Timeout (5s)
Wait 1 second
Attempt 2 → Timeout (5s)
Wait 2 seconds
Attempt 3 → Success
Total time: ~13 seconds
Result: Order created (after retries)
```

### Scenario 3: Inventory Service is Down ❌
```
Attempt 1 → Error (service down)
Wait 1 second
Attempt 2 → Error (service down)
Wait 2 seconds
Attempt 3 → Error (service down)
Total time: ~3 seconds
Result: Clear error to user (not a silent failure)
```

## 🧪 Running Tests in Detail

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=OrderServiceTest#testCreateOrder_Success
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
open target/site/jacoco/index.html
```

## 📝 Test Cases Explained

### Test 1: ✅ Successful Order
- **What**: Inventory service responds immediately
- **How**: Mock `checkInventory()` to return success
- **Verify**: Order created with correct details

### Test 2: ❌ Failed Order
- **What**: Inventory service is down
- **How**: Mock `checkInventory()` to throw exception
- **Verify**: ServiceUnavailableException thrown

### Test 3: 🔍 Service Called
- **What**: Verify inventory service was called
- **How**: Use `verify()` to check method calls
- **Verify**: `checkInventory()` called exactly once

### Test 4: 📊 Order Details
- **What**: All order fields set correctly
- **How**: Create order and check each field
- **Verify**: ID, product, quantity, status, timestamp

## 🎓 Learning Resources

### Concepts Covered
1. **Retry Pattern**: Try multiple times before failing
2. **Timeout Handling**: Set limits on waiting
3. **Exponential Backoff**: Increase wait time between retries
4. **Dependency Injection**: Spring @Autowired
5. **Unit Testing**: JUnit 5 + Mockito
6. **Exception Handling**: Custom exceptions
7. **Logging**: SLF4J with logback

### Microservices Patterns Implemented
- ✅ Retry Pattern
- ✅ Timeout Pattern
- ✅ Circuit Breaker (ready in code comments)
- ✅ Graceful Degradation
- ✅ Observability (logging)

## 🐛 Common Issues and Solutions

### Issue: "Connection refused" Error
**Problem**: Inventory Service not running on port 8081

**Solution**: 
```bash
# Start a mock inventory service
python3 -c "from http.server import *; HTTPServer(('', 8081), SimpleHTTPRequestHandler).serve_forever()"
```

### Issue: Tests Fail
**Problem**: RestTemplate not properly configured

**Solution**: Run `mvn clean install` to ensure dependencies are downloaded

### Issue: Port Already in Use
**Problem**: Another service running on 8080

**Solution**: Change port in `application.properties`:
```properties
server.port=8082
```

## 📚 Next Steps

1. **Add Database**: Use Spring Data JPA to persist orders
2. **Add API Validation**: Use @Valid and Bean Validation
3. **Add Metrics**: Use Micrometer for Prometheus metrics
4. **Add Circuit Breaker**: Implement Hystrix/Resilience4j
5. **Add Authentication**: Use Spring Security
6. **Deploy**: Docker + Kubernetes

## 🤝 Contributing

Feel free to fork, modify, and use this for learning!

## 📄 License

MIT License - Use freely for learning and projects

## 👤 Author

Created by GitHub Copilot for learning Microservices and Resilience Patterns

---

**Questions?** Read the code comments - they explain everything! 🎓

**Want to learn more?** Check out:
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Microservices Patterns](https://microservices.io/patterns/)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
