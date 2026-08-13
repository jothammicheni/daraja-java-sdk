package com.github.jothammicheni.daraja.client;

import com.github.jothammicheni.daraja.config.MpesaConfig;
import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.exception.MpesaApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultMpesaClientTest {

    private MpesaConfig config;
    private DefaultMpesaClient client;

    @BeforeEach
    void setUp() {
        config = new MpesaConfig.Builder(
            "test-consumer-key",
            "test-consumer-secret",
            "test-api-secret"
        )
        .baseUrl("https://sandbox.safaricom.co.ke")
        .callbackUrl("https://myapp.com/cb/confirmation")
        .connectTimeout(5)
        .readTimeout(10)
        .build();

        client = new DefaultMpesaClient(config);
    }

    @Test
    void shouldThrowExceptionWhenIdempotencyKeyIsNull() {
        StkPushRequest request = new StkPushRequest(
            "174379",
            "254708374149",
            100,
            "254708374149",
            "174379",
            "ORDER-123",
            "Payment",
            null
        );

        assertThatThrownBy(() -> client.initiateStkPush(request))
            .isInstanceOf(MpesaApiException.class)
            .hasMessage("Idempotency-Key is mandatory to prevent duplicate payments");
    }

    @Test
    void shouldThrowExceptionWhenIdempotencyKeyIsBlank() {
        StkPushRequest request = new StkPushRequest(
            "174379",
            "254708374149",
            100,
            "254708374149",
            "174379",
            "ORDER-123",
            "Payment",
            ""
        );

        assertThatThrownBy(() -> client.initiateStkPush(request))
            .isInstanceOf(MpesaApiException.class)
            .hasMessage("Idempotency-Key is mandatory to prevent duplicate payments");
    }

    @Test
    void shouldCreateClientWithConfig() {
        assertThat(client).isNotNull();
    }
}