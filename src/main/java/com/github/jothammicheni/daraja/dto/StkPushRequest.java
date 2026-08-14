package com.github.jothammicheni.daraja.dto;

import com.github.jothammicheni.daraja.util.PhoneNumberUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * Request object for initiating an STK Push payment.
 * Use the {@link Builder} for a clean, readable way to create requests.
 */
public record StkPushRequest(
        String businessShortCode,
        String phoneNumber,
        int amount,
        String partyA,
        String partyB,
        String accountReference,
        String transactionDesc,
        String idempotencyKey
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Compact constructor with validation.
     * Validates phone number, amount, and idempotency key.
     */
    public StkPushRequest {
        // Validate phone number
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            if (!PhoneNumberUtils.isValidKenyanPhone(phoneNumber)) {
                throw new IllegalArgumentException(
                        "Invalid Kenyan phone number: " + PhoneNumberUtils.maskPhoneNumber(phoneNumber) +
                                ". Must be in format: +2547XXXXXXXX, 2547XXXXXXXX, 07XXXXXXXX, or 7XXXXXXXX"
                );
            }
        } else {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Validate amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        // Validate idempotency key
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key is mandatory to prevent duplicate payments");
        }
    }

    /**
     * Returns the phone number normalized to M-Pesa format (2547XXXXXXXX).
     */
    public String getNormalizedPhoneNumber() {
        return PhoneNumberUtils.normalizeKenyanPhone(phoneNumber);
    }

    /**
     * Returns the masked phone number for logging.
     */
    public String getMaskedPhoneNumber() {
        return PhoneNumberUtils.maskPhoneNumber(phoneNumber);
    }

    /**
     * Returns the phone type (prepaid/postpaid).
     */
    public PhoneNumberUtils.PhoneType getPhoneType() {
        return PhoneNumberUtils.detectPhoneType(phoneNumber);
    }

    // ============================================
    // BUILDER PATTERN - Clean way to create requests
    // ============================================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String businessShortCode;
        private String phoneNumber;
        private int amount;
        private String partyA;
        private String partyB;
        private String accountReference;
        private String transactionDesc;
        private String idempotencyKey;

        private Builder() {}

        // Fluent setters
        public Builder businessShortCode(String businessShortCode) {
            this.businessShortCode = businessShortCode;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder partyA(String partyA) {
            this.partyA = partyA;
            return this;
        }

        public Builder partyB(String partyB) {
            this.partyB = partyB;
            return this;
        }

        public Builder accountReference(String accountReference) {
            this.accountReference = accountReference;
            return this;
        }

        public Builder description(String description) {
            this.transactionDesc = description;
            return this;
        }

        public Builder transactionDesc(String transactionDesc) {
            this.transactionDesc = transactionDesc;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        // ✅ Auto-normalize phone number
        public Builder phoneNormalized(String phoneNumber) {
            this.phoneNumber = PhoneNumberUtils.normalizeKenyanPhone(phoneNumber);
            return this;
        }

        /**
         * Builds the StkPushRequest with auto-fill magic.
         * - Auto-generates idempotency key if not provided
         * - Auto-fills PartyA = phoneNumber if not provided
         * - Auto-fills PartyB = businessShortCode if not provided
         * - Auto-fills description if not provided
         */
        public StkPushRequest build() {
            // ✅ Auto-generate idempotency key if not set
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                idempotencyKey = UUID.randomUUID().toString();
            }

            // ✅ Auto-fill Party A (defaults to phoneNumber)
            if (partyA == null || partyA.isBlank()) {
                partyA = phoneNumber;
            }

            // ✅ Auto-fill Party B (defaults to businessShortCode)
            if (partyB == null || partyB.isBlank()) {
                partyB = businessShortCode;
            }

            // Auto-fill description if not set
            if (transactionDesc == null || transactionDesc.isBlank()) {
                transactionDesc = "Payment for " + accountReference;
            }

            // Validate required fields
            if (businessShortCode == null || businessShortCode.isBlank()) {
                throw new IllegalArgumentException("Business ShortCode is required");
            }
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new IllegalArgumentException("Phone Number is required");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be greater than 0");
            }
            if (accountReference == null || accountReference.isBlank()) {
                throw new IllegalArgumentException("Account Reference is required");
            }

            return new StkPushRequest(
                    businessShortCode,
                    phoneNumber,
                    amount,
                    partyA,
                    partyB,
                    accountReference,
                    transactionDesc,
                    idempotencyKey
            );
        }
    }
}