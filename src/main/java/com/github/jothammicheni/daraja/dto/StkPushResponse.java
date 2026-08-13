package com.github.jothammicheni.daraja.dto;

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
) implements Serializable {
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
                merchantRequestID,
                checkoutRequestID,
                responseCode,
                responseDescription,
                customerMessage,
                key,
                timestamp
        );
    }
}