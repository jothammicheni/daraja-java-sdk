package com.github.jothammicheni.daraja.dashboard;

import java.time.Instant;

/**
 * Represents a single webhook log entry.
 * Pure Java DTO - no framework dependencies.
 */
public class WebhookLogEntry {

    private final String id;
    private final Instant timestamp;
    private final String status;
    private final String resultCode;
    private final String resultDescription;
    private final String amount;
    private final String phoneNumber;
    private final String maskedPhoneNumber;
    private final String receiptNumber;
    private final String checkoutId;
    private final String merchantId;
    private final String accountReference;
    private final String rawJson;

    public WebhookLogEntry(String id, Instant timestamp, String status, String resultCode,
                           String resultDescription, String amount, String phoneNumber,
                           String maskedPhoneNumber, String receiptNumber, String checkoutId,
                           String merchantId, String accountReference, String rawJson) {
        this.id = id;
        this.timestamp = timestamp;
        this.status = status;
        this.resultCode = resultCode;
        this.resultDescription = resultDescription;
        this.amount = amount;
        this.phoneNumber = phoneNumber;
        this.maskedPhoneNumber = maskedPhoneNumber;
        this.receiptNumber = receiptNumber;
        this.checkoutId = checkoutId;
        this.merchantId = merchantId;
        this.accountReference = accountReference;
        this.rawJson = rawJson;
    }

    // Getters
    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getResultCode() { return resultCode; }
    public String getResultDescription() { return resultDescription; }
    public String getAmount() { return amount; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMaskedPhoneNumber() { return maskedPhoneNumber; }
    public String getReceiptNumber() { return receiptNumber; }
    public String getCheckoutId() { return checkoutId; }
    public String getMerchantId() { return merchantId; }
    public String getAccountReference() { return accountReference; }
    public String getRawJson() { return rawJson; }
}