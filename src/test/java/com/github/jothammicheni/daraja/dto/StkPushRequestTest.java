package com.github.jothammicheni.daraja.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StkPushRequestTest {

    @Test
    void shouldCreateValidStkPushRequest() {
        StkPushRequest request = new StkPushRequest(
            "174379",
            "254708374149",
            100,
            "254708374149",
            "174379",
            "ORDER-123",
            "Payment for order",
            "idempotent-key-1"
        );

        assertThat(request.businessShortCode()).isEqualTo("174379");
        assertThat(request.phoneNumber()).isEqualTo("254708374149");
        assertThat(request.amount()).isEqualTo(100);
        assertThat(request.partyA()).isEqualTo("254708374149");
        assertThat(request.partyB()).isEqualTo("174379");
        assertThat(request.accountReference()).isEqualTo("ORDER-123");
        assertThat(request.transactionDesc()).isEqualTo("Payment for order");
        assertThat(request.idempotencyKey()).isEqualTo("idempotent-key-1");
    }

    @Test
    void shouldSupportNullIdempotencyKey() {
        StkPushRequest request = new StkPushRequest(
            "174379",
            "254708374149",
            100,
            "254708374149",
            "174379",
            "ORDER-123",
            "Payment for order",
            null
        );

        assertThat(request.idempotencyKey()).isNull();
    }

    @Test
    void shouldBeSerializable() {
        StkPushRequest request = new StkPushRequest(
            "174379",
            "254708374149",
            100,
            "254708374149",
            "174379",
            "ORDER-123",
            "Payment for order",
            "idempotent-key-1"
        );

        assertThat(request).isInstanceOf(java.io.Serializable.class);
    }
}