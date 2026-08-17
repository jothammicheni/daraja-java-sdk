package com.github.jothammicheni.daraja.event;

import java.time.Instant;

/**
 * Base class for all M-Pesa events.
 * All events contain common metadata like timestamp and checkout ID.
 */
public abstract class MpesaEvent {

    private final String checkoutId;
    private final String merchantId;
    private final Instant timestamp;
    private final EventType type;

    public enum EventType {
        PAYMENT_INITIATED,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        PAYMENT_CANCELLED,
        TOKEN_REFRESHED,
        WEBHOOK_RECEIVED
    }

    protected MpesaEvent(String checkoutId, String merchantId, EventType type) {
        this.checkoutId = checkoutId;
        this.merchantId = merchantId;
        this.timestamp = Instant.now();
        this.type = type;
    }

    public String getCheckoutId() { return checkoutId; }
    public String getMerchantId() { return merchantId; }
    public Instant getTimestamp() { return timestamp; }
    public EventType getType() { return type; }

    @Override
    public String toString() {
        return String.format("%s{checkoutId='%s', merchantId='%s', timestamp=%s}",
                getClass().getSimpleName(), checkoutId, merchantId, timestamp);
    }
}