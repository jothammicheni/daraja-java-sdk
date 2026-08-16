package com.github.jothammicheni.daraja.dto;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class StkPushResponseTest {

    @Test
    void shouldCreateValidStkPushResponse() {
        StkPushResponse response = new StkPushResponse(
            "MER-123",
            "CHECK-456",
            "0",
            "Success",
            "Payment received",
            "idempotent-key-1",
            Instant.now()
        );

        assertThat(response.merchantRequestID()).isEqualTo("MER-123");
        assertThat(response.checkoutRequestID()).isEqualTo("CHECK-456");
        assertThat(response.responseCode()).isEqualTo("0");
        assertThat(response.responseDescription()).isEqualTo("Success");
        assertThat(response.customerMessage()).isEqualTo("Payment received");
        assertThat(response.idempotencyKey()).isEqualTo("idempotent-key-1");
    }

    @Test
    void shouldSetTimestampAutomaticallyIfNull() {
        StkPushResponse response = new StkPushResponse(
            "MER-123",
            "CHECK-456",
            "0",
            "Success",
            "Payment received",
            "idempotent-key-1",
            null
        );

        assertThat(response.timestamp()).isNotNull();
        assertThat(response.timestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldReturnTrueForAcceptedResponse() {
        StkPushResponse response = new StkPushResponse(
            "MER-123",
            "CHECK-456",
            "0",
            "Success",
            "Payment received",
            "idempotent-key-1",
            Instant.now()
        );

        assertThat(response.isAccepted()).isTrue();
        assertThat(response.isRejected()).isFalse();
    }

    @Test
    void shouldReturnFalseForRejectedResponse() {
        StkPushResponse response = new StkPushResponse(
            "MER-123",
            "CHECK-456",
            "1",
            "Insufficient funds",
            "Failed",
            "idempotent-key-1",
            Instant.now()
        );

        assertThat(response.isAccepted()).isFalse();
        assertThat(response.isRejected()).isTrue();
    }

    @Test
    void shouldCreateNewResponseWithIdempotencyKey() {
        StkPushResponse original = new StkPushResponse(
            "MER-123",
            "CHECK-456",
            "0",
            "Success",
            "Payment received",
            null,
            Instant.now()
        );

        StkPushResponse updated = original.withIdempotencyKey("new-key");

        assertThat(updated.idempotencyKey()).isEqualTo("new-key");
        assertThat(updated.merchantRequestID()).isEqualTo(original.merchantRequestID());
        assertThat(updated.checkoutRequestID()).isEqualTo(original.checkoutRequestID());
    }

    @Test
    void shouldBeSerializable() {
        StkPushResponse response = new StkPushResponse(
            "MER-123",
            "CHECK-456",
            "0",
            "Success",
            "Payment received",
            "idempotent-key-1",
            Instant.now()
        );

        assertThat(response).isInstanceOf(java.io.Serializable.class);
    }
}