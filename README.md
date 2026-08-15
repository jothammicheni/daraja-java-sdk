```markdown
# Daraja M-Pesa SDK

[![JitPack](https://img.shields.io/badge/JitPack-1.0.0-blue)](https://jitpack.io/#jothammicheni/daraja-java-sdk)
[![Java Version](https://img.shields.io/badge/Java-17+-orange)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](https://opensource.org/licenses/Apache-2.0)

A production-ready, pure Java SDK for Safaricom's Daraja M-Pesa API. Built for Java 17+ with **zero external dependencies**, it handles OAuth token management, idempotency, phone number validation, and webhook parsing so you don't have to.

> ⚠️ **Sandbox by default.** This SDK ships configured for Safaricom's sandbox environment. Don't point it at production credentials until you've explicitly set `MPESA_ENVIRONMENT=production` and verified your configuration — mixing sandbox keys with production endpoints (or vice versa) will fail or misbehave.

---

## Table of Contents

- [Why this SDK](#why-this-sdk)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Building a Payment Flow](#building-a-payment-flow)
- [API Performance Logging](#api-performance-logging)
- [Event-Driven Architecture (Optional)](#event-driven-architecture-optional)
- [Sandbox Testing](#sandbox-testing)
- [Error Handling](#error-handling)
- [Security](#security)
- [Contributing](#contributing)
- [Support](#support)

---

## Why this SDK

- **Pure Java 17+** — no Spring or framework lock-in required
- **Zero external dependencies** — no Jackson, no bloat
- **Fluent builder API** — construct requests with smart auto-fill defaults
- **Automatic token management** — OAuth acquisition and refresh handled for you
- **Built-in idempotency** — duplicate-payment protection out of the box, with optional Redis backing for multi-instance deployments
- **Full Kenyan phone number support** — `+254`, `254`, `07`, `7`, `01` formats all normalize correctly
- **Enterprise-grade webhook security** — IP validation and structured, readable logs
- **~290 fewer lines of boilerplate** per integration compared to hand-rolling this yourself
- **Optional event system** — react to payment outcomes without writing webhook-handling logic
- **Built-in webhook dashboard** — real-time performance logging with zero external services

---

## Installation

**Maven** — add the JitPack repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the dependency. Pin to a release for anything beyond local testing:

```xml
<dependency>
    <groupId>com.github.jothammicheni</groupId>
    <artifactId>daraja-java-sdk</artifactId>
    <version>v1.0.0</version>
</dependency>
```

<details>
<summary>Using the latest development build instead</summary>

```xml
<dependency>
    <groupId>com.github.jothammicheni</groupId>
    <artifactId>daraja-java-sdk</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```
</details>

<details>
<summary>Gradle</summary>

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.jothammicheni:daraja-java-sdk:v1.0.0'
}
```
</details>

---

## Quick Start

1. Create a `.env` file in your project root (never commit this — add it to `.gitignore`):

```env
MPESA_CONSUMER_KEY=your_consumer_key
MPESA_CONSUMER_SECRET=your_consumer_secret
MPESA_API_SECRET=your_api_passkey
MPESA_APP_URL=https://your-app.onrender.com
MPESA_CALLBACK_URL=https://your-app.onrender.com/api/payment/callback
```

2. Initialize the client:

```java
import com.github.jothammicheni.daraja.client.MpesaClient;
import com.github.jothammicheni.daraja.client.DefaultMpesaClient;
import com.github.jothammicheni.daraja.config.MpesaConfig;
import com.github.jothammicheni.daraja.config.MpesaEnvironment;

MpesaConfig config = new MpesaConfig.Builder(
        System.getenv("MPESA_CONSUMER_KEY"),
        System.getenv("MPESA_CONSUMER_SECRET"),
        System.getenv("MPESA_API_SECRET")
)
        .environment(MpesaEnvironment.valueOf(
                System.getenv().getOrDefault("MPESA_ENVIRONMENT", "sandbox").toUpperCase()
        ))
        .appUrl(System.getenv("MPESA_APP_URL"))
        .callbackUrl(System.getenv("MPESA_CALLBACK_URL"))
        .build();

MpesaClient client = new DefaultMpesaClient(config);
```

3. Send a payment request:

```java
import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;

StkPushRequest request = StkPushRequest.builder()
        .businessShortCode("174379")
        .phoneNumber("254708374149")
        .amount(100)
        .accountReference("ORDER-123")
        .build(); // idempotencyKey is auto-generated

StkPushResponse response = client.initiateStkPush(request);

if (response.isAccepted()) {
    System.out.println("✅ Checkout ID: " + response.checkoutRequestID());
} else {
    System.out.println("❌ Rejected: " + response.responseDescription());
}
```

That's the whole loop for a first payment. See [Building a Payment Flow](#building-a-payment-flow) for handling the webhook callback and a complete controller example.

---

## Configuration

### Environment Variables

| Variable | Required | Default | Description |
| :--- | :--- | :--- | :--- |
| `MPESA_CONSUMER_KEY` | ✅ | — | M-Pesa API consumer key |
| `MPESA_CONSUMER_SECRET` | ✅ | — | M-Pesa API consumer secret |
| `MPESA_API_SECRET` | ✅ | — | API passkey, used for webhook validation |
| `MPESA_APP_URL` | ✅ | — | Your application's public URL |
| `MPESA_CALLBACK_URL` | ✅ | — | Full webhook callback URL |
| `MPESA_ENVIRONMENT` | – | `sandbox` | `sandbox` or `production` |
| `MPESA_CACHE_TYPE` | – | `local` | `local` (in-memory) or `redis` (shared) |
| `MPESA_CONNECT_TIMEOUT` | – | `10` | Connection timeout, in seconds |
| `MPESA_READ_TIMEOUT` | – | `30` | Read timeout, in seconds |
| `MPESA_ENABLE_IP_VALIDATION` | – | `false` | Validate webhook source IP |
| `MPESA_BEHIND_PROXY` | – | `false` | Set `true` if your app sits behind a reverse proxy |
| `MPESA_CONFIRMATION_URL_PATH` | – | `/cb/confirmation` | Confirmation webhook path |
| `MPESA_VALIDATION_URL_PATH` | – | `/cb/validation` | Validation webhook path |

> 🔒 **Never commit your `.env` file.** These credentials grant access to a live payment system.

<details>
<summary>Full <code>.env</code> template</summary>

```env
# Required — from the Safaricom Developer Portal
MPESA_CONSUMER_KEY=your_consumer_key
MPESA_CONSUMER_SECRET=your_consumer_secret
MPESA_API_SECRET=your_api_passkey

# Required — application URLs
MPESA_APP_URL=https://your-app.onrender.com
MPESA_CALLBACK_URL=https://your-app.onrender.com/api/payment/callback

# Optional
MPESA_ENVIRONMENT=sandbox
MPESA_CACHE_TYPE=local
MPESA_CONNECT_TIMEOUT=10
MPESA_READ_TIMEOUT=30
MPESA_ENABLE_IP_VALIDATION=false
MPESA_BEHIND_PROXY=false
MPESA_CONFIRMATION_URL_PATH=/cb/confirmation
MPESA_VALIDATION_URL_PATH=/cb/validation
```
</details>

### Spring Boot

<details>
<summary><code>application.yml</code></summary>

```yaml
mpesa:
  daraja:
    consumer-key: ${MPESA_CONSUMER_KEY}
    consumer-secret: ${MPESA_CONSUMER_SECRET}
    api-secret: ${MPESA_API_SECRET}
    app-url: ${MPESA_APP_URL}
    callback-url: ${MPESA_CALLBACK_URL}
    environment: ${MPESA_ENVIRONMENT:sandbox}
    cache-type: ${MPESA_CACHE_TYPE:local}
    connect-timeout: ${MPESA_CONNECT_TIMEOUT:10}
    read-timeout: ${MPESA_READ_TIMEOUT:30}
    enable-ip-validation: ${MPESA_ENABLE_IP_VALIDATION:false}
    is-behind-proxy: ${MPESA_BEHIND_PROXY:false}
    confirmation-url-path: ${MPESA_CONFIRMATION_URL_PATH:/cb/confirmation}
    validation-url-path: ${MPESA_VALIDATION_URL_PATH:/cb/validation}
```
</details>

<details>
<summary><code>application.properties</code></summary>

```properties
mpesa.daraja.consumer-key=${MPESA_CONSUMER_KEY}
mpesa.daraja.consumer-secret=${MPESA_CONSUMER_SECRET}
mpesa.daraja.api-secret=${MPESA_API_SECRET}
mpesa.daraja.app-url=${MPESA_APP_URL}
mpesa.daraja.callback-url=${MPESA_CALLBACK_URL}
mpesa.daraja.environment=${MPESA_ENVIRONMENT:sandbox}
mpesa.daraja.cache-type=${MPESA_CACHE_TYPE:local}
mpesa.daraja.connect-timeout=${MPESA_CONNECT_TIMEOUT:10}
mpesa.daraja.read-timeout=${MPESA_READ_TIMEOUT:30}
mpesa.daraja.enable-ip-validation=${MPESA_ENABLE_IP_VALIDATION:false}
mpesa.daraja.is-behind-proxy=${MPESA_BEHIND_PROXY:false}
mpesa.daraja.confirmation-url-path=${MPESA_CONFIRMATION_URL_PATH:/cb/confirmation}
mpesa.daraja.validation-url-path=${MPESA_VALIDATION_URL_PATH:/cb/validation}
```
</details>

<details>
<summary>Sandbox vs. production profiles</summary>

**Sandbox (development)**
```properties
mpesa.daraja.environment=sandbox
mpesa.daraja.cache-type=local
mpesa.daraja.enable-ip-validation=false
mpesa.daraja.app-url=http://localhost:8080
mpesa.daraja.callback-url=http://localhost:8080/api/payment/callback
```

**Production (live payments)**
```properties
# ⚠️ Only enable after verifying every value below
mpesa.daraja.environment=production
mpesa.daraja.cache-type=redis
mpesa.daraja.enable-ip-validation=true
mpesa.daraja.is-behind-proxy=true
mpesa.daraja.app-url=https://your-production-domain.com
mpesa.daraja.callback-url=https://your-production-domain.com/api/payment/callback

spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.password=${REDIS_PASSWORD}
```
</details>

### Redis (Optional)

The SDK uses an **in-memory local cache** by default — fine for single-instance deployments. For production with multiple instances, switch to Redis so token and idempotency caches are shared across them.

```env
MPESA_CACHE_TYPE=redis
```

| Cache Type | Token Cache | Idempotency Cache | Best For |
| :--- | :--- | :--- | :--- |
| `local` | Per instance | Per instance | Development, single instance |
| `redis` | Shared | Shared | Production, multiple instances |

<details>
<summary>Redis connection settings + Docker setup</summary>

```properties
mpesa.daraja.cache-type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=your-redis-password
spring.data.redis.timeout=5000ms

# Connection pool (optional)
spring.data.redis.lettuce.pool.max-active=10
spring.data.redis.lettuce.pool.max-idle=5
spring.data.redis.lettuce.pool.min-idle=2
```

```bash
# Quick local Redis
docker run -d -p 6379:6379 --name redis redis:alpine
```
</details>

---

## Building a Payment Flow

### 1. Initiate a payment

```java
import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;

StkPushRequest request = StkPushRequest.builder()
        .businessShortCode("174379")      // Your PayBill/Till number
        .phoneNumber("254708374149")      // Any Kenyan format works
        .amount(100)                      // Amount in KES
        .accountReference("ORDER-123")
        .build();                         // idempotencyKey auto-generated

StkPushResponse response = client.initiateStkPush(request);
```

The builder fills in the details you'd otherwise repeat every time: it generates a unique `idempotencyKey` if you don't supply one (so a retried request can't double-charge a customer), sets `PartyA`/`PartyB` from the phone number and short code, and falls back to a sensible default `description`.

### 2. Handle the webhook callback

Safaricom's STK Push callback logs automatically — no code required for that part:

```
📨 CONFIRMATION WEBHOOK
   Time: 2026-08-15T07:33:00.050Z
   Status: ✅ SUCCESS
   Checkout ID: ws_CO_150820261032480113730593
   Amount: 100.00
   Receipt: UHF6Z36OYN
```

Parse it into a structured object and branch on the result:

```java
import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;
import com.github.jothammicheni.daraja.dto.webhook.WebhookResponse;
import com.github.jothammicheni.daraja.webhook.WebhookParser;

@PostMapping("/api/payment/callback")
public ResponseEntity<Map<String, String>> handleCallback(@RequestBody Map<String, Object> rawPayload) {
    WebhookPayload payload = WebhookParser.parseWebhook(rawPayload);

    if (payload.isSuccess()) {
        // Update order status, send confirmation, release inventory, etc.
    } else if ("1032".equals(payload.getResultCode())) {
        // User cancelled the prompt
    } else {
        // Payment failed — see payload.getResultDescription()
    }

    return ResponseEntity.ok(WebhookResponse.success().toMap());
}
```

> **Note:** the STK Push callback identifies the transaction by `CheckoutRequestID`, not by your account reference — Safaricom doesn't echo that field back on this webhook. Keep your own `checkoutRequestID → orderId` mapping (registered right after `initiateStkPush()` succeeds) so you can look the order back up when the callback arrives.

### 3. Full controller example

```java
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
        orderService.createOrder(request.getOrderId(), request.getPhoneNumber(), request.getAmount());

        StkPushRequest stkRequest = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getAmount())
                .accountReference(request.getOrderId())
                .description("Payment for order: " + request.getOrderId())
                .build();

        StkPushResponse response = mpesaClient.initiateStkPush(stkRequest);
        orderService.registerCheckoutId(response.checkoutRequestID(), request.getOrderId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> handleCallback(@RequestBody Map<String, Object> rawPayload) {
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

## API Performance Logging

The SDK includes a built-in webhook dashboard that automatically logs all incoming M-Pesa callbacks in real time, giving you instant visibility into your payment flows.

![Webhook Dashboard](docs/images/log-dashboard)

### How It Works

The dashboard captures every webhook your application receives and displays it in a clean, filterable interface. No external services, no database setup — just add a few lines of code and visit `/dashboard.html`.

| Feature | Description |
| :--- | :--- |
| **Real-time monitoring** | Webhooks appear automatically as they arrive |
| **Auto-refresh** | Dashboard updates every 5 seconds |
| **Status filtering** | Filter by Success, Failed, Cancelled, or Pending |
| **Raw JSON view** | Click any entry to inspect the full payload |
| **Live statistics** | Real-time counts for Total, Success, Failed, and Cancelled |
| **In-memory storage** | Stores the last 1,000 webhooks |
| **Zero dependencies** | Pure HTML/JS dashboard — no framework required |

### 1. Enable Dashboard Storage

Add **one line** to your webhook handler:

```java
import com.github.jothammicheni.daraja.dashboard.WebhookLogStorage;

@PostMapping("/api/payment/callback")
public ResponseEntity<Map<String, String>> handleCallback(@RequestBody Map<String, Object> rawPayload) {
    WebhookPayload payload = WebhookParser.parseWebhook(rawPayload);

    // ✅ One line — stores the webhook for the dashboard
    WebhookLogStorage.store(payload, rawPayload.toString());

    // ... your existing business logic
    return ResponseEntity.ok(WebhookResponse.success().toMap());
}
```

### 2. Add Dashboard Endpoints (Spring Boot)

```java
package com.example.controller;

import com.github.jothammicheni.daraja.dashboard.WebhookLogEntry;
import com.github.jothammicheni.daraja.dashboard.WebhookLogStorage;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook-dashboard")
public class WebhookDashboardController {

    @GetMapping("/logs")
    public List<WebhookLogEntry> getLogs(@RequestParam(defaultValue = "100") int limit) {
        return WebhookLogStorage.getRecentLogs(limit);
    }

    @GetMapping("/stats")
    public Map<String, Integer> getStats() {
        return WebhookLogStorage.getStats();
    }

    @DeleteMapping("/logs")
    public Map<String, String> clearLogs() {
        WebhookLogStorage.clearLogs();
        return Map.of("status", "cleared");
    }
}
```

### 3. Visit the Dashboard

```
http://localhost:8080/dashboard.html
```

### What You'll See

```
📨 Webhook Dashboard

📊 Total: 6   ✅ Success: 2   ❌ Failed: 2   🚫 Cancelled: 2

📋 All  ✅ Success  ❌ Failed  🚫 Cancelled  ⏳ Pending  🔄 Refresh  🗑️ Clear

Time                Checkout ID                    Status     Amount  Phone       Receipt      Result
8/15, 12:29 PM     ws_CO_150820261229192113730593  FAILED     N/A     N/A         N/A          17
8/15, 12:29 PM     ws_CO_150820261229037113730593  SUCCESS    1       ******0593  UHF6Z371PA   0
8/15, 12:28 PM     ws_CO_150820261228314113730593  FAILED     N/A     N/A         N/A          1037
8/15, 12:28 PM     ws_CO_150820261228167113730593  CANCELLED  N/A     N/A         N/A          1032
8/15, 12:27 PM     ws_CO_150820261226520113730593  CANCELLED  N/A     N/A         N/A          1032
8/15, 12:26 PM     ws_CO_150820261226377113730593  SUCCESS    1       ******0593  UHF6Z375QH   0
```

### Dashboard Features

| Feature | How It Works |
| :--- | :--- |
| **Auto-refresh** | Updates every 5 seconds automatically |
| **Filter by status** | Click any status badge to filter logs |
| **View raw JSON** | Click the 📄 button to see the full M-Pesa callback payload |
| **Clear logs** | Use the 🗑️ Clear button to reset the dashboard |
| **Live stats** | Counts update in real-time |

### Configuration

The dashboard stores the last **1,000** webhooks in memory. Adjust `MAX_LOGS` in `WebhookLogStorage.java` if needed:

```java
private static final int MAX_LOGS = 500;  // Store 500 logs instead of 1000
```

> 💡 **Note:** The dashboard is designed for debugging and monitoring during development. For long-term storage or analytics, implement a database-backed persistence layer.

---

## Event-Driven Architecture (Optional)

If you'd rather keep business logic out of your controller entirely, the SDK ships an optional event system.

**Without events**, everything ends up crammed into the callback handler:

```java
@PostMapping("/callback")
public ResponseEntity<Map<String, String>> handleCallback(@RequestBody Map<String, Object> rawPayload) {
    WebhookPayload payload = WebhookParser.parseWebhook(rawPayload);
    if (payload.isSuccess()) {
        orderService.markAsPaid(payload.getAccountReference());
        emailService.sendConfirmation(payload.getPhoneNumber());
        inventoryService.release(payload.getAccountReference());
        analyticsService.trackPayment(payload);
    }
    return ResponseEntity.ok(WebhookResponse.success().toMap());
}
```

**With events**, the controller just parses and publishes:

```java
@PostMapping("/callback")
public ResponseEntity<Map<String, String>> handleCallback(@RequestBody Map<String, Object> rawPayload) {
    WebhookPayload payload = WebhookParser.parseWebhook(rawPayload);
    MpesaEventPublisher.getInstance().publishWebhookEvent(payload);
    return ResponseEntity.ok(WebhookResponse.success().toMap());
}
```

...and the actual business logic lives in a listener, wherever it belongs in your codebase:

```java
@Component
public class PaymentEventListener {

    @EventListener
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        orderService.markAsPaid(event.getAccountReference());
        emailService.sendConfirmation(event.getPhoneNumber());
        inventoryService.release(event.getAccountReference());
        analyticsService.trackPayment(event);
    }

    @EventListener
    public void onPaymentFailed(PaymentFailedEvent event) {
        orderService.markAsFailed(event.getAccountReference());
        notificationService.notifyUser(event.getPhoneNumber(), event.getResultDescription());
    }

    @EventListener
    public void onPaymentCancelled(PaymentCancelledEvent event) {
        orderService.markAsCancelled(event.getAccountReference());
        notificationService.notifyUser(event.getPhoneNumber(), "You cancelled the payment");
    }
}
```

---

## Sandbox Testing

> ⚠️ Sandbox only — do not use these values with production credentials.

**Test phone numbers:**

| Phone Number | Type |
| :--- | :--- |
| `254708374149` | Prepaid |
| `254113730593` | Postpaid |

On the simulated prompt, enter PIN **`12345`** and select **`1`** to confirm.

**Result codes you'll see while testing:**

| Scenario | Result Code |
| :--- | :--- |
| Successful payment | `0` |
| User cancellation | `1032` |
| Insufficient funds | `2001` |
| Timeout (no response) | `1020` |
| Amount exceeds allowed limit | `17` |

---

## Error Handling

| Code | Meaning | Suggested action |
| :--- | :--- | :--- |
| `0` | Success | Mark payment complete |
| `1032` | User cancelled | Update order to `CANCELLED` |
| `1020` | Timeout | Allow the user to retry |
| `2001` | Insufficient funds | Notify the user |
| `17` | Rule limited | Check your amount limits |

```java
try {
    StkPushResponse response = client.initiateStkPush(request);
} catch (MpesaApiException e) {
    System.err.println("Error code: " + e.getErrorCode().getCode());
    System.err.println("Message: " + e.getMessage());
} catch (Exception e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

---

## Security

Found a vulnerability? Please don't open a public issue.

1. Email [jothammurimi21@gmail.com](mailto:jothammurimi21@gmail.com) with details and steps to reproduce
2. We'll acknowledge within 24 hours and share an estimated fix timeline
3. Please keep the report private until a patch ships

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make sure tests pass
4. Document any new behavior
5. Open a pull request against `dev`

---

## Support

- **Issues:** [GitHub Issues](https://github.com/jothammicheni/daraja-java-sdk/issues)
- **Discussions:** [GitHub Discussions](https://github.com/jothammicheni/daraja-java-sdk/discussions)
- **Email:** [jothammurimi21@gmail.com](mailto:jothammurimi21@gmail.com)

---

Licensed under [Apache License 2.0](LICENSE).
```