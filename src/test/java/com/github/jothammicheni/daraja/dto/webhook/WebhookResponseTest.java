package com.github.jothammicheni.daraja.dto.webhook;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        WebhookResponse response = WebhookResponse.success();
        assertThat(response.getResultCode()).isEqualTo("0");
        assertThat(response.getResultDesc()).isEqualTo("Success");
    }

    @Test
    void shouldCreateSuccessResponseWithCustomMessage() {
        WebhookResponse response = WebhookResponse.success("Payment accepted");
        assertThat(response.getResultCode()).isEqualTo("0");
        assertThat(response.getResultDesc()).isEqualTo("Payment accepted");
    }

    @Test
    void shouldCreateFailureResponse() {
        WebhookResponse response = WebhookResponse.failure("Invalid request");
        assertThat(response.getResultCode()).isEqualTo("1");
        assertThat(response.getResultDesc()).isEqualTo("Invalid request");
    }

    @Test
    void shouldCreateFailureResponseWithCustomCode() {
        WebhookResponse response = WebhookResponse.failure("1032", "User cancelled");
        assertThat(response.getResultCode()).isEqualTo("1032");
        assertThat(response.getResultDesc()).isEqualTo("User cancelled");
    }

    @Test
    void shouldConvertToMap() {
        WebhookResponse response = WebhookResponse.success();
        Map<String, String> map = response.toMap();
        assertThat(map).containsEntry("ResultCode", "0")
                .containsEntry("ResultDesc", "Success");
    }

    @Test
    void shouldHaveToString() {
        WebhookResponse response = WebhookResponse.success();
        assertThat(response.toString()).contains("WebhookResponse{code='0', desc='Success'}");
    }
}