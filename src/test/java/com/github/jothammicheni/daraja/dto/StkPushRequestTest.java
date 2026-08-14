package com.github.jothammicheni.daraja.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StkPushRequestTest {

    @Test
    void shouldCreateValidStkPushRequest() {
        String idempotencyKey = UUID.randomUUID().toString();

        StkPushRequest request = new StkPushRequest(
                "174379",
                "254708374149",
                100,
                "254708374149",
                "174379",
                "ORDER-123",
                "Payment for order",
                idempotencyKey
        );

        assertThat(request.businessShortCode()).isEqualTo("174379");
        assertThat(request.phoneNumber()).isEqualTo("254708374149");
        assertThat(request.amount()).isEqualTo(100);
        assertThat(request.partyA()).isEqualTo("254708374149");
        assertThat(request.partyB()).isEqualTo("174379");
        assertThat(request.accountReference()).isEqualTo("ORDER-123");
        assertThat(request.transactionDesc()).isEqualTo("Payment for order");
        assertThat(request.idempotencyKey()).isEqualTo(idempotencyKey);
    }

    @Test
    void shouldThrowExceptionWhenIdempotencyKeyIsNull() {
        assertThatThrownBy(() -> new StkPushRequest(
                "174379",
                "254708374149",
                100,
                "254708374149",
                "174379",
                "ORDER-123",
                "Payment for order",
                null  // ❌ Null idempotency key
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Idempotency-Key is mandatory to prevent duplicate payments");
    }

    @Test
    void shouldThrowExceptionWhenIdempotencyKeyIsBlank() {
        assertThatThrownBy(() -> new StkPushRequest(
                "174379",
                "254708374149",
                100,
                "254708374149",
                "174379",
                "ORDER-123",
                "Payment for order",
                ""  // ❌ Blank idempotency key
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Idempotency-Key is mandatory to prevent duplicate payments");
    }

    @Test
    void shouldThrowExceptionWhenPhoneNumberIsInvalid() {
        assertThatThrownBy(() -> new StkPushRequest(
                "174379",
                "invalid-phone",
                100,
                "invalid-phone",
                "174379",
                "ORDER-123",
                "Payment for order",
                UUID.randomUUID().toString()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Kenyan phone number");
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        assertThatThrownBy(() -> new StkPushRequest(
                "174379",
                "254708374149",
                0,  // ❌ Zero amount
                "254708374149",
                "174379",
                "ORDER-123",
                "Payment for order",
                UUID.randomUUID().toString()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than 0");
    }

    @Test
    void shouldAutoGenerateIdempotencyKeyWithBuilder() {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        assertThat(request.idempotencyKey()).isNotNull();
        assertThat(request.idempotencyKey()).isNotEmpty();
    }

    @Test
    void shouldAutoFillPartyAAndPartyBWithBuilder() {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        assertThat(request.partyA()).isEqualTo("254708374149");  // Auto-filled
        assertThat(request.partyB()).isEqualTo("174379");         // Auto-filled
    }

    @Test
    void shouldAutoFillDescriptionWithBuilder() {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        assertThat(request.transactionDesc()).isEqualTo("Payment for ORDER-123");
    }

    @Test
    void shouldOverrideAutoFilledFieldsWhenProvided() {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .partyA("254700000000")
                .partyB("600000")
                .accountReference("ORDER-123")
                .description("Custom description")
                .idempotencyKey("custom-key-123")
                .build();

        assertThat(request.partyA()).isEqualTo("254700000000");
        assertThat(request.partyB()).isEqualTo("600000");
        assertThat(request.transactionDesc()).isEqualTo("Custom description");
        assertThat(request.idempotencyKey()).isEqualTo("custom-key-123");
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
                UUID.randomUUID().toString()
        );

        assertThat(request).isInstanceOf(java.io.Serializable.class);
    }
}