package com.github.jothammicheni.daraja.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.time.Instant;

public record StkPushResponse(
        @JsonAlias("MerchantRequestID") String merchantRequestID,
        @JsonAlias("CheckoutRequestID") String checkoutRequestID,
        @JsonAlias("ResponseCode") String responseCode,
        @JsonAlias("ResponseDescription") String responseDescription,
        @JsonAlias("CustomerMessage") String customerMessage,
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