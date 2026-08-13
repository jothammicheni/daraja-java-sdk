package com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;

public record StkPushResponse(
        @JsonProperty("MerchantRequestID") String merchantRequestID,
        @JsonProperty("CheckoutRequestID") String checkoutRequestID,
        @JsonProperty("ResponseCode") String responseCode,
        @JsonProperty("ResponseDescription") String responseDescription,
        @JsonProperty("CustomerMessage") String customerMessage,
        String idempotencyKey,
        Instant timestamp
) implements Serializable { // ⚡ FIXED: Mandatory for saving into AWS Redis Idempotency Cache

    // ⚡ FIXED: Ensures data stream compatibility when other developers upgrade your library versions
    private static final long serialVersionUID = 1L;

    public StkPushResponse {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public boolean isAccepted() {
        return "0".equals(responseCode);
    }

    public boolean isRejected() {
        return !isAccepted();
    }

    public StkPushResponse withIdempotencyKey(String key) {
        return new StkPushResponse(
                this.merchantRequestID,
                this.checkoutRequestID,
                this.responseCode,
                this.responseDescription,
                this.customerMessage,
                key,
                this.timestamp
        );
    }
}
