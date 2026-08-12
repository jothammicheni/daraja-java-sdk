# Daraja M-Pesa Spring Boot Starter

[![Maven Central](https://shields.io)](https://maven.org)
[![Java Version](https://shields.io)](https://oracle.com)
[![Spring Boot](https://shields.io)](https://spring.io)
[![License](https://shields.io)](https://opensource.org)

A modern, cloud-native Spring Boot Starter SDK for Safaricom's Daraja M-Pesa API. Built natively using Java 21 Records, featuring robust distributed **Idempotency guards via Redis**, **automated proxy-aware IP validation matching Safaricom subnets**, and **Spring ApplicationEvent streaming**.

---

## 🚀 Key Features

* **Zero Boilerplate:** Autoconfigures seamlessly out of the box with your host properties.
* **Modern Java Architecture:** Built using clean, type-safe Java 21 compact record constructors.
* **Double-Debit Prevention:** Implements true atomic idempotency controls across single or multi-instance systems.
* **Enterprise Security:** Built-in protection against IP spoofing that accurately verifies Safaricom production gateway subnets.
* **Asynchronous Webhook Streaming:** Automatically converts ugly, deeply nested Daraja callbacks into flat Java events.

---

## 📦 1. Installation

Add the following dependency to your Spring Boot application's `pom.xml`:

```xml
<dependency>
    <groupId>com.github.jothammicheni</groupId>
    <artifactId>daraja-springboot-starter-jdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## ⚙️ 2. Properties Configuration

Configure your environment settings inside your application's `application.properties` or `application.yml` file.

### Option A: Local Sandbox Staging
Perfect for local development. This mode uses local in-memory caching and skips gateway IP checking so you can test endpoints from your laptop.

```properties
# Safaricom Developer Credentials
mpesa.daraja.consumer-key=your_sandbox_consumer_key
mpesa.daraja.consumer-secret=your_sandbox_consumer_secret
mpesa.daraja.api-secret=your_sandbox_passkey

# Target Environment & Domain Routing
mpesa.daraja.environment=sandbox
mpesa.daraja.app-url=https://ngrok-free.app

# Infrastructure Settings (Local Fallback)
mpesa.daraja.cache-type=local
mpesa.daraja.enable-ip-validation=false
mpesa.daraja.is-behind-proxy=false

# Optional Route Customization (Defaults to /cb/confirmation)
mpesa.daraja.confirmation-url-path=/v1/mpesa/payment-hook
```

### Option B: Real Money Production
Use this setup on live production servers. It enforces Redis distributed state tracking and turns on the network firewalls.

```properties
# Live Production Credentials from Daraja Portal
mpesa.daraja.consumer-key=LIVE_PRODUCTION_CONSUMER_KEY_FROM_SAFARICOM
mpesa.daraja.consumer-secret=LIVE_PRODUCTION_CONSUMER_SECRET_FROM_SAFARICOM
mpesa.daraja.api-secret=LIVE_PRODUCTION_LIPA_NA_MPESA_PASSKEY

# Target Environment (Automatically switches base to https://safaricom.co.ke)
mpesa.daraja.environment=production
mpesa.daraja.app-url=https://yourcompany.co.ke

# Infrastructure Settings (Distributed Cloud Multi-Instance ready)
mpesa.daraja.cache-type=redis
mpesa.daraja.enable-ip-validation=true
mpesa.daraja.is-behind-proxy=true

# Custom Route Path
mpesa.daraja.confirmation-url-path=/v1/mpesa/payment-hook

# Connect your application's standard Redis Server configuration
spring.data.redis.host=://amazonaws.com
spring.data.redis.port=6379
```

---

## 💻 3. Quickstart Code Snippets

### Step 1: Trigger an M-Pesa STK Push
Inject the `MpesaClient` directly into your business service to trigger a phone STK prompt. Always provide a unique `idempotencyKey` per customer request to prevent duplicate transactions.

```java
package com.yourcompany.billing.service;

import com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto.StkPushRequest;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto.StkPushResponse;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.service.MpesaClient;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessingService {

    private final MpesaClient mpesaClient;

    public PaymentProcessingService(MpesaClient mpesaClient) {
        this.mpesaClient = mpesaClient;
    }

    public void collectPayment(String orderId, String phoneNumber, double amount) {
        // Build the type-safe request payload
        StkPushRequest request = new StkPushRequest(
                "req-idempotency-" + orderId, // ⚡ Unique Key prevents accidental double clicking!
                "174379",                     // Business Short Code (PayBill/Till)
                amount,
                phoneNumber,                  // e.g., 2547XXXXXXXX
                orderId,                      // Account Reference
                "Payment for Order " + orderId
        );

        // Send request to Safaricom Daraja
        StkPushResponse response = mpesaClient.initiateStkPush(request);

        if (response.isAccepted()) {
            System.out.println("Prompt sent to phone! CheckoutRequestID: " + response.checkoutRequestID());
        } else {
            System.err.println("Safaricom rejected request early: " + response.responseDescription());
        }
    }
}
```

### Step 2: Listen for Payment Completions
You do not need to wire up controller endpoints manually. When Safaricom returns a payment callback, this starter processes it and broadcasts it across your application. Simply attach an `@EventListener` to catch the payment result:

```java
package com.yourcompany.billing.listener;

import com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto.MpesaPaymentResult;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MpesaCallbackListener {

    @EventListener
    public void onMpesaPaymentReceived(MpesaPaymentResult payment) {
        String checkoutId = payment.checkoutRequestId();

        if (payment.isSuccess()) {
            System.out.println("💰 CASH RECEIVED!");
            System.out.println("M-Pesa Receipt Number: " + payment.mpesaReceiptNumber());
            System.out.println("Amount Collected: KES " + payment.amount());
            System.out.println("Customer Mobile: " + payment.phoneNumber());
            
            // TODO: Update your internal database status to "PAID" here!
        } else {
            System.err.println("❌ Transaction Failed or Cancelled by User.");
            System.err.println("Reason: " + payment.resultDesc() + " (Code: " + payment.resultCode() + ")");
        }
    }
}
```

---

## 🔒 Security Policy & Disclosures

We take security issues very seriously. If you find a security vulnerability within this starter library, please **do not open a public GitHub issue**. Instead, follow our responsible disclosure policy.

* **Reporting Email:** Please send your vulnerability findings directly to [jothammicheni@gmail.com](mailto:jothammicheni@gmail.com).
* **Expected Response:** We will acknowledge your email within 24 hours and provide an estimated fix timeline.
* **Coordination:** We ask that you keep information private until we publish a patch release to protect production platforms using this SDK.

---

## 📄 License

This starter library is open-source software licensed under the [Apache License, Version 2.0](LICENSE).
