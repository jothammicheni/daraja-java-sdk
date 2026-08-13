package com.github.jothammicheni.daraja.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MpesaApiExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        MpesaApiException exception = new MpesaApiException("Test error message");

        assertThat(exception.getMessage()).isEqualTo("Test error message");
        assertThat(exception.getErrorCode()).isEqualTo(MpesaErrorCode.GENERIC_ERROR);
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        MpesaApiException exception = new MpesaApiException("Test error", cause);

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getErrorCode()).isEqualTo(MpesaErrorCode.GENERIC_ERROR);
    }

    @Test
    void shouldCreateExceptionWithErrorCodeAndMessage() {
        MpesaApiException exception = new MpesaApiException(
            MpesaErrorCode.AUTH_FAILED,
            "Authentication failed"
        );

        assertThat(exception.getMessage()).isEqualTo("Authentication failed");
        assertThat(exception.getErrorCode()).isEqualTo(MpesaErrorCode.AUTH_FAILED);
    }

    @Test
    void shouldThrowExceptionAndCatchIt() {
        assertThatThrownBy(() -> {
            throw new MpesaApiException("Test exception");
        })
        .isInstanceOf(MpesaApiException.class)
        .hasMessage("Test exception");
    }
}