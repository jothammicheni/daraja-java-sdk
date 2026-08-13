package com.github.jothammicheni.daraja.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MpesaErrorCodeTest {

    @Test
    void shouldHaveCorrectErrorCodes() {
        assertThat(MpesaErrorCode.GENERIC_ERROR.getCode()).isEqualTo("MPESA-001");
        assertThat(MpesaErrorCode.AUTH_FAILED.getCode()).isEqualTo("MPESA-002");
        assertThat(MpesaErrorCode.INVALID_REQUEST.getCode()).isEqualTo("MPESA-003");
        assertThat(MpesaErrorCode.WEBHOOK_SIGNATURE_INVALID.getCode()).isEqualTo("MPESA-004");
        assertThat(MpesaErrorCode.IDEMPOTENCY_VIOLATION.getCode()).isEqualTo("MPESA-005");
    }

    @Test
    void shouldHaveCorrectDefaultMessages() {
        assertThat(MpesaErrorCode.GENERIC_ERROR.getDefaultMessage()).isEqualTo("An unexpected error occurred");
        assertThat(MpesaErrorCode.AUTH_FAILED.getDefaultMessage()).isEqualTo("Authentication with M-Pesa failed");
        assertThat(MpesaErrorCode.INVALID_REQUEST.getDefaultMessage()).isEqualTo("Invalid request payload");
        assertThat(MpesaErrorCode.WEBHOOK_SIGNATURE_INVALID.getDefaultMessage()).isEqualTo("Webhook signature validation failed");
        assertThat(MpesaErrorCode.IDEMPOTENCY_VIOLATION.getDefaultMessage()).isEqualTo("Duplicate request detected");
    }

    @Test
    void shouldHaveFiveErrorCodes() {
        assertThat(MpesaErrorCode.values()).hasSize(5);
    }
}