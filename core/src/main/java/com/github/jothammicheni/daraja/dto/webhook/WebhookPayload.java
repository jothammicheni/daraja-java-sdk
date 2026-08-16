package com.github.jothammicheni.daraja.dto.webhook;

import java.io.Serializable;
import java.time.Instant;

/**
 * Pure Java DTO - No Spring annotations!
 */
public class WebhookPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private String merchantRequestId;
    private String checkoutRequestId;
    private String resultCode;
    private String resultDescription;
    private String transactionId;
    private String transactionDate;
    private String amount;
    private String phoneNumber;
    private String maskedPhoneNumber;
    private String businessShortCode;
    private String accountReference;
    private String receiptNumber;
    private String customerMessage;
    private Instant receivedAt;

    // Private constructor for builder
    private WebhookPayload() {}

    // Getters
    public String getMerchantRequestId() { return merchantRequestId; }
    public String getCheckoutRequestId() { return checkoutRequestId; }
    public String getResultCode() { return resultCode; }
    public String getResultDescription() { return resultDescription; }
    public String getTransactionId() { return transactionId; }
    public String getTransactionDate() { return transactionDate; }
    public String getAmount() { return amount; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMaskedPhoneNumber() { return maskedPhoneNumber; }
    public String getBusinessShortCode() { return businessShortCode; }
    public String getAccountReference() { return accountReference; }
    public String getReceiptNumber() { return receiptNumber; }
    public String getCustomerMessage() { return customerMessage; }
    public Instant getReceivedAt() { return receivedAt; }

    public boolean isSuccess() {
        return "0".equals(resultCode);
    }

    public boolean isFailed() {
        return !isSuccess();
    }

    public String getSummary() {
        return String.format(
                "Payment %s: %s %s from %s",
                isSuccess() ? "SUCCESS" : "FAILED",
                amount != null ? amount : "N/A",
                transactionId != null ? "(ID: " + transactionId + ")" : "",
                maskedPhoneNumber != null ? maskedPhoneNumber : "N/A"
        );
    }

    @Override
    public String toString() {
        return String.format(
                "WebhookPayload{transactionId='%s', amount='%s', success=%s}",
                transactionId, amount, isSuccess()
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final WebhookPayload payload = new WebhookPayload();

        public Builder merchantRequestId(String value) {
            payload.merchantRequestId = value;
            return this;
        }

        public Builder checkoutRequestId(String value) {
            payload.checkoutRequestId = value;
            return this;
        }

        public Builder resultCode(String value) {
            payload.resultCode = value;
            return this;
        }

        public Builder resultDescription(String value) {
            payload.resultDescription = value;
            return this;
        }

        public Builder transactionId(String value) {
            payload.transactionId = value;
            return this;
        }

        public Builder transactionDate(String value) {
            payload.transactionDate = value;
            return this;
        }

        public Builder amount(String value) {
            payload.amount = value;
            return this;
        }

        public Builder phoneNumber(String value) {
            payload.phoneNumber = value;
            if (value != null) {
                payload.maskedPhoneNumber = maskPhoneNumber(value);
            }
            return this;
        }

        public Builder businessShortCode(String value) {
            payload.businessShortCode = value;
            return this;
        }

        public Builder accountReference(String value) {
            payload.accountReference = value;
            return this;
        }

        public Builder receiptNumber(String value) {
            payload.receiptNumber = value;
            return this;
        }

        public Builder customerMessage(String value) {
            payload.customerMessage = value;
            return this;
        }

        public Builder receivedAt(Instant value) {
            payload.receivedAt = value;
            return this;
        }

        public WebhookPayload build() {
            if (payload.receivedAt == null) {
                payload.receivedAt = Instant.now();
            }
            return payload;
        }

        private String maskPhoneNumber(String phone) {
            if (phone == null || phone.length() < 4) return "****";
            String cleaned = phone.replaceAll("[^\\d]", "");
            if (cleaned.length() < 4) return "****";
            return "******" + cleaned.substring(cleaned.length() - 4);
        }
    }
}