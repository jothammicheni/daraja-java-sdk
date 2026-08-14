# Daraja M-Pesa SDK

[![JitPack](https://img.shields.io/badge/JitPack-1.0.0-blue)](https://jitpack.io/#jothammicheni/daraja-java-sdk)
[![Java Version](https://img.shields.io/badge/Java-17+-orange)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](https://opensource.org/licenses/Apache-2.0)

A production-ready, pure Java SDK for Safaricom's Daraja M-Pesa API. Built for Java 17+ with zero external dependencies, this SDK handles OAuth token management, idempotency, phone validation, and webhook parsing with enterprise-grade security.

> **⚠️ IMPORTANT DISCLAIMER: This SDK is currently configured for SANDBOX testing only. Do not use production credentials with this SDK until you have explicitly set `MPESA_ENVIRONMENT=production` and verified all configurations. Using sandbox keys with production endpoints will fail, and using production keys with sandbox endpoints may result in unexpected behavior.**

---

## Key Features

- **Pure Java 17+**: No Spring or framework dependencies required
- **Zero External Dependencies**: Pure Java, no Jackson or other libraries
- **Clean Builder Pattern**: Fluent API for constructing requests with smart auto-fill
- **Automatic Token Management**: Handles OAuth token acquisition and refresh
- **Built-in Idempotency**: Prevents duplicate payments with Redis or local cache
- **Phone Number Validation**: Supports all Kenyan formats (+254, 254, 07, 7, 01)
- **Enterprise Security**: IP validation for webhook sources, signature verification
- **Beautiful Logging**: Clean, structured webhook logs out of the box
- **Distributed Cache Ready**: Redis support for multi-instance deployments
- **97% Code Reduction**: SDK handles ~290 lines of boilerplate code

---

## Installation

### JitPack Configuration

Add JitPack repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### Maven Dependency

For the latest development version:
```xml
<dependency>
    <groupId>com.github.jothammicheni</groupId>
    <artifactId>daraja-java-sdk</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

For a specific release:
```xml
<dependency>
    <groupId>com.github.jothammicheni</groupId>
    <artifactId>daraja-java-sdk</artifactId>
    <version>v1.0.0</version>
</dependency>
```

### Gradle

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.jothammicheni:daraja-java-sdk:main-SNAPSHOT'
}
```

---

## Environment Configuration (.env File)

### Required Environment Variables

> **⚠️ SECURITY WARNING**: Never commit your `.env` file to version control. Add `.env` to your `.gitignore`. These credentials provide access to payment systems.

Create a `.env` file in your project root:

```env
# ============================================
# REQUIRED - M-Pesa API Credentials
# Obtain from Safaricom Developer Portal
# ============================================
MPESA_CONSUMER_KEY=your_consumer_key
MPESA_CONSUMER_SECRET=your_consumer_secret
MPESA_API_SECRET=your_api_passkey

# ============================================
# REQUIRED - Application URLs
# ============================================
MPESA_APP_URL=https://your-app.onrender.com
MPESA_CALLBACK_URL=https://your-app.onrender.com/api/payment/callback

# ============================================
# OPTIONAL - Environment (default: sandbox)
# Set to "production" for live payments
# ============================================
MPESA_ENVIRONMENT=sandbox

# ============================================
# OPTIONAL - Cache Configuration
# Default: local (in-memory)
# Set to "redis" for distributed caching
# ============================================
MPESA_CACHE_TYPE=local

# ============================================
# OPTIONAL - Timeouts (seconds)
# ============================================
MPESA_CONNECT_TIMEOUT=10
MPESA_READ_TIMEOUT=30

# ============================================
# OPTIONAL - Security Settings
# ============================================
MPESA_ENABLE_IP_VALIDATION=false
MPESA_BEHIND_PROXY=false

# ============================================
# OPTIONAL - Webhook Paths
# ============================================
MPESA_CONFIRMATION_URL_PATH=/cb/confirmation
MPESA_VALIDATION_URL_PATH=/cb/validation
```

### Environment Variable Reference

| Variable | Required | Default | Description |
| :--- | :--- | :--- | :--- |
| `MPESA_CONSUMER_KEY` | ✅ Yes | - | Your M-Pesa API consumer key |
| `MPESA_CONSUMER_SECRET` | ✅ Yes | - | Your M-Pesa API consumer secret |
| `MPESA_API_SECRET` | ✅ Yes | - | Your API passkey for webhook validation |
| `MPESA_APP_URL` | ✅ Yes | - | Your application's public URL |
| `MPESA_CALLBACK_URL` | ✅ Yes | - | Full webhook callback URL |
| `MPESA_ENVIRONMENT` | ❌ No | `sandbox` | `sandbox` or `production` |
| `MPESA_CACHE_TYPE` | ❌ No | `local` | `local` or `redis` |
| `MPESA_CONNECT_TIMEOUT` | ❌ No | `10` | Connection timeout in seconds |
| `MPESA_READ_TIMEOUT` | ❌ No | `30` | Read timeout in seconds |
| `MPESA_ENABLE_IP_VALIDATION` | ❌ No | `false` | Validate webhook source IP |
| `MPESA_BEHIND_PROXY` | ❌ No | `false` | Application behind a proxy |
| `MPESA_CONFIRMATION_URL_PATH` | ❌ No | `/cb/confirmation` | Confirmation webhook path |
| `MPESA_VALIDATION_URL_PATH` | ❌ No | `/cb/validation` | Validation webhook path |

---

## Spring Boot Configuration

### application.properties

```properties
# ============================================
# M-Pesa SDK Configuration
# ============================================

# Required - M-Pesa API Credentials
# WARNING: Never hardcode credentials in properties files
# Always use environment variables
mpesa.daraja.consumer-key=${MPESA_CONSUMER_KEY}
mpesa.daraja.consumer-secret=${MPESA_CONSUMER_SECRET}
mpesa.daraja.api-secret=${MPESA_API_SECRET}

# Required - Application URLs
mpesa.daraja.app-url=${MPESA_APP_URL}
mpesa.daraja.callback-url=${MPESA_CALLBACK_URL}

# Optional - Environment (sandbox or production)
mpesa.daraja.environment=${MPESA_ENVIRONMENT:sandbox}

# Optional - Cache Type (local or redis)
# Default: local (in-memory)
mpesa.daraja.cache-type=${MPESA_CACHE_TYPE:local}

# Optional - Timeouts (seconds)
mpesa.daraja.connect-timeout=${MPESA_CONNECT_TIMEOUT:10}
mpesa.daraja.read-timeout=${MPESA_READ_TIMEOUT:30}

# Optional - Security Settings
mpesa.daraja.enable-ip-validation=${MPESA_ENABLE_IP_VALIDATION:false}
mpesa.daraja.is-behind-proxy=${MPESA_BEHIND_PROXY:false}

# Optional - Webhook Paths
mpesa.daraja.confirmation-url-path=${MPESA_CONFIRMATION_URL_PATH:/cb/confirmation}
mpesa.daraja.validation-url-path=${MPESA_VALIDATION_URL_PATH:/cb/validation}
```

### application.yml

```yaml
# ============================================
# M-Pesa SDK Configuration
# ============================================

mpesa:
  daraja:
    # Required - M-Pesa API Credentials
    # WARNING: Never hardcode credentials
    consumer-key: ${MPESA_CONSUMER_KEY}
    consumer-secret: ${MPESA_CONSUMER_SECRET}
    api-secret: ${MPESA_API_SECRET}
    
    # Required - Application URLs
    app-url: ${MPESA_APP_URL}
    callback-url: ${MPESA_CALLBACK_URL}
    
    # Optional - Environment (sandbox or production)
    environment: ${MPESA_ENVIRONMENT:sandbox}
    
    # Optional - Cache Type (local or redis)
    # Default: local (in-memory)
    cache-type: ${MPESA_CACHE_TYPE:local}
    
    # Optional - Timeouts (seconds)
    connect-timeout: ${MPESA_CONNECT_TIMEOUT:10}
    read-timeout: ${MPESA_READ_TIMEOUT:30}
    
    # Optional - Security Settings
    enable-ip-validation: ${MPESA_ENABLE_IP_VALIDATION:false}
    is-behind-proxy: ${MPESA_BEHIND_PROXY:false}
    
    # Optional - Webhook Paths
    confirmation-url-path: ${MPESA_CONFIRMATION_URL_PATH:/cb/confirmation}
    validation-url-path: ${MPESA_VALIDATION_URL_PATH:/cb/validation}
```

### Environment-Specific Profiles

#### Sandbox (Development)
```properties
mpesa.daraja.environment=sandbox
mpesa.daraja.cache-type=local
mpesa.daraja.enable-ip-validation=false
mpesa.daraja.app-url=http://localhost:8080
mpesa.daraja.callback-url=http://localhost:8080/api/payment/callback
```

#### Production (Live Payments)
```properties
# ⚠️ WARNING: Only use after verifying all configurations
mpesa.daraja.environment=production
mpesa.daraja.cache-type=redis
mpesa.daraja.enable-ip-validation=true
mpesa.daraja.is-behind-proxy=true
mpesa.daraja.app-url=https://your-production-domain.com
mpesa.daraja.callback-url=https://your-production-domain.com/api/payment/callback

# Redis Configuration (Required for cache-type=redis)
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.password=${REDIS_PASSWORD}
```

---

## Redis Configuration

### Redis is Optional - Local Cache is Default

By default, the SDK uses **in-memory local cache** (`cache-type=local`). This works for single-instance deployments. For production with multiple instances, enable Redis.

### Enable Redis Cache

In your `.env` file:
```env
MPESA_CACHE_TYPE=redis
```

### Spring Boot Redis Configuration

```properties
# Enable Redis
mpesa.daraja.cache-type=redis

# Redis Connection
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=your-redis-password
spring.data.redis.timeout=5000ms

# Redis Connection Pool (Optional)
spring.data.redis.lettuce.pool.max-active=10
spring.data.redis.lettuce.pool.max-idle=5
spring.data.redis.lettuce.pool.min-idle=2
```

### Docker Redis Setup

```bash
# Start Redis with Docker
docker run -d -p 6379:6379 --name redis redis:alpine

# Or with Docker Compose
cat > docker-compose.yml << 'EOF'
version: '3.8'
services:
  redis:
    image: redis:alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
volumes:
  redis-data:
EOF

docker-compose up -d
```

### Cache Behavior

| Cache Type | Token Cache | Idempotency Cache | Best For |
| :--- | :--- | :--- | :--- |
| **Local** | Per instance | Per instance | Development, single instance |
| **Redis** | Shared across instances | Shared across instances | Production, multiple instances |

---

## Sandbox Testing

> **⚠️ DISCLAIMER: The following instructions are for SANDBOX testing only. Do not use with production credentials.**

### Test Phone Numbers

Use these test phone numbers in sandbox environment:

| Phone Number | Type | Description |
| :--- | :--- | :--- |
| `254708374149` | Prepaid | Works for all test scenarios |
| `254113730593` | Postpaid | Works for all test scenarios |

### Test PIN

When prompted on the test phone:
- Enter `12345` as the PIN
- Select `1` to confirm payment

### Test Scenarios

| Scenario | Result Code | Description |
| :--- | :--- | :--- |
| Successful Payment | `0` | User enters correct PIN |
| User Cancellation | `1032` | User cancels the transaction |
| Insufficient Funds | `2001` | User has insufficient balance |
| Timeout | `1020` | User does not respond in time |
| Rule Limited | `17` | Amount exceeds allowed limits |

### Quick Start with Sandbox

```java
// Configuration automatically loads from .env
MpesaConfig config = new MpesaConfig.Builder(
    System.getenv("MPESA_CONSUMER_KEY"),
    System.getenv("MPESA_CONSUMER_SECRET"),
    System.getenv("MPESA_API_SECRET")
)
.environment(MpesaEnvironment.SANDBOX)  // Explicitly use sandbox
.appUrl(System.getenv("MPESA_APP_URL"))
.callbackUrl(System.getenv("MPESA_CALLBACK_URL"))
.build();

MpesaClient client = new DefaultMpesaClient(config);
```

---

## Quick Start

### 1. Initialize the Client

```java
import com.github.jothammicheni.daraja.client.MpesaClient;
import com.github.jothammicheni.daraja.client.DefaultMpesaClient;
import com.github.jothammicheni.daraja.config.MpesaConfig;
import com.github.jothammicheni.daraja.config.MpesaEnvironment;

// All configuration loaded from .env
MpesaConfig config = new MpesaConfig.Builder(
    System.getenv("MPESA_CONSUMER_KEY"),
    System.getenv("MPESA_CONSUMER_SECRET"),
    System.getenv("MPESA_API_SECRET")
)
.environment(MpesaEnvironment.valueOf(
    System.getenv("MPESA_ENVIRONMENT", "sandbox").toUpperCase()
))
.appUrl(System.getenv("MPESA_APP_URL"))
.callbackUrl(System.getenv("MPESA_CALLBACK_URL"))
.cacheType(System.getenv("MPESA_CACHE_TYPE", "local"))
.enableIpValidation(Boolean.parseBoolean(
    System.getenv("MPESA_ENABLE_IP_VALIDATION", "false")
))
.isBehindProxy(Boolean.parseBoolean(
    System.getenv("MPESA_BEHIND_PROXY", "false")
))
.connectTimeout(Integer.parseInt(
    System.getenv("MPESA_CONNECT_TIMEOUT", "10")
))
.readTimeout(Integer.parseInt(
    System.getenv("MPESA_READ_TIMEOUT", "30")
))
.build();

MpesaClient client = new DefaultMpesaClient(config);
```

### 2. Initiate STK Push Payment (Builder Pattern)

> **✨ Builder Pattern Benefits**: The builder automatically handles PartyA, PartyB, idempotency key, and description - you only need to provide the essentials!

```java
import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;

// ✅ Using Builder Pattern - Clean, readable, auto-fill
StkPushRequest request = StkPushRequest.builder()
    .businessShortCode("174379")          // Your PayBill/Till number
    .phoneNumber("254708374149")          // Any Kenyan format works
    .amount(100)                          // Amount in KES
    .accountReference("ORDER-123")        // Your reference
    // .idempotencyKey("custom-key")     // Optional - auto-generated if omitted
    // .description("Custom description") // Optional - auto-generated if omitted
    .build();

StkPushResponse response = client.initiateStkPush(request);

if (response.isAccepted()) {
    System.out.println("✅ Checkout ID: " + response.checkoutRequestID());
    System.out.println("📱 Check your phone for the STK Push prompt.");
} else {
    System.out.println("❌ Request rejected: " + response.responseDescription());
}
```

**The builder automatically handles:**
- ✅ Generates a unique `idempotencyKey` if not provided (prevents duplicate payments)
- ✅ Sets `PartyA` as the `phoneNumber` (customer's phone)
- ✅ Sets `PartyB` as the `businessShortCode` (your business)
- ✅ Provides a default `description` if omitted

**Example with custom values:**
```java
StkPushRequest request = StkPushRequest.builder()
    .businessShortCode("174379")
    .phoneNumber("254708374149")
    .amount(100)
    .partyA("254700000000")              // Override PartyA
    .partyB("600000")                    // Override PartyB
    .accountReference("ORDER-123")
    .description("Custom payment description")
    .idempotencyKey("my-custom-key-123") // Custom idempotency key
    .build();
```

### 3. Handle Webhook Callback

```java
import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;
import com.github.jothammicheni.daraja.dto.webhook.WebhookResponse;
import com.github.jothammicheni.daraja.webhook.WebhookParser;
import com.github.jothammicheni.daraja.webhook.WebhookLogger;

@PostMapping("/api/payment/callback")
public ResponseEntity<Map<String, String>> handleCallback(@RequestBody Map<String, Object> rawPayload) {
    // SDK logs the webhook automatically
    WebhookLogger.logWebhook(rawPayload, "CONFIRMATION");
    
    // SDK parses the webhook into clean structured data
    WebhookPayload payload = WebhookParser.parseWebhook(rawPayload);
    
    // Check payment status
    if (payload.isSuccess()) {
        System.out.println("Payment successful!");
        System.out.println("Transaction ID: " + payload.getTransactionId());
        System.out.println("Amount: " + payload.getAmount());
        System.out.println("Receipt: " + payload.getReceiptNumber());
        
        // YOUR BUSINESS LOGIC HERE
        // - Update order status to "PAID"
        // - Send confirmation email
        // - Release inventory
    } else if ("1032".equals(payload.getResultCode())) {
        System.out.println("User cancelled the payment");
        // Handle cancellation
    } else {
        System.out.println("Payment failed: " + payload.getResultDescription());
        // Handle failure
    }
    
    // Return response to M-Pesa
    return ResponseEntity.ok(WebhookResponse.success().toMap());
}
```

---

## Complete Spring Boot Example with Builder

```java
package com.example.payment;

import com.github.jothammicheni.daraja.client.MpesaClient;
import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;
import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;
import com.github.jothammicheni.daraja.dto.webhook.WebhookResponse;
import com.github.jothammicheni.daraja.webhook.WebhookLogger;
import com.github.jothammicheni.daraja.webhook.WebhookParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    
    private final MpesaClient mpesaClient;
    private final OrderService orderService;
    
    public PaymentController(MpesaClient mpesaClient, OrderService orderService) {
        this.mpesaClient = mpesaClient;
        this.orderService = orderService;
    }
    
    @PostMapping("/initiate")
    public ResponseEntity<StkPushResponse> initiatePayment(@RequestBody PaymentRequest request) {
        // Create order with PENDING status
        orderService.createOrder(request.getOrderId(), request.getPhoneNumber(), request.getAmount());
        
        // ✅ Build STK Push request using Builder pattern with auto-fill
        StkPushRequest stkRequest = StkPushRequest.builder()
            .businessShortCode("174379")
            .phoneNumber(request.getPhoneNumber())
            .amount(request.getAmount())
            .accountReference(request.getOrderId())
            .description("Payment for order: " + request.getOrderId())
            .build();  // ✅ idempotencyKey auto-generated!
        
        // Send STK Push
        StkPushResponse response = mpesaClient.initiateStkPush(stkRequest);
        
        // Link checkout ID to order
        orderService.registerCheckoutId(response.checkoutRequestID(), request.getOrderId());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> handleCallback(@RequestBody Map<String, Object> rawPayload) {
        // SDK handles parsing and logging
        WebhookLogger.logWebhook(rawPayload, "CONFIRMATION");
        WebhookPayload payload = WebhookParser.parseWebhook(rawPayload);
        
        if (payload.isSuccess()) {
            orderService.markAsPaid(payload);
        } else if ("1032".equals(payload.getResultCode())) {
            orderService.markAsCancelled(payload);
        } else {
            orderService.markAsFailed(payload);
        }
        
        return ResponseEntity.ok(WebhookResponse.success().toMap());
    }
}
```

---

## Error Handling

### Common Result Codes

| Code | Description | Action |
| :--- | :--- | :--- |
| `0` | Success | Payment completed |
| `1032` | User Cancelled | Update order to CANCELLED |
| `1020` | Timeout | Allow retry |
| `2001` | Insufficient Funds | Notify user |
| `17` | Rule Limited | Check amount limits |

### Exception Handling

```java
try {
StkPushResponse response = client.initiateStkPush(request);
} catch (MpesaApiException e) {
        // Handle SDK-specific errors
        System.err.println("Error Code: " + e.getErrorCode().getCode());
        System.err.println("Message: " + e.getMessage());
        } catch (Exception e) {
        // Handle general errors
        System.err.println("Unexpected error: " + e.getMessage());
        }
```

---

## Security Policy

### Reporting Vulnerabilities

If you discover a security vulnerability, please do not open a public issue. Instead:

1. Email your findings to: [jothammicheni@gmail.com](mailto:jothammicheni@gmail.com)
2. We will acknowledge within 24 hours
3. We will provide an estimated fix timeline
4. We request that you keep information private until a patch is released

### Responsible Disclosure

- Please provide detailed steps to reproduce the issue
- Include affected versions
- Suggest a fix if possible

---

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Submit a pull request to the `dev` branch
4. Ensure all tests pass
5. Include documentation for new features

---

## Support

- **Issues**: [GitHub Issues](https://github.com/jothammicheni/daraja-java-sdk/issues)
- **Discussions**: [GitHub Discussions](https://github.com/jothammicheni/daraja-java-sdk/discussions)
- **Email**: [jothammicheni@gmail.com](mailto:jothammicheni@gmail.com)