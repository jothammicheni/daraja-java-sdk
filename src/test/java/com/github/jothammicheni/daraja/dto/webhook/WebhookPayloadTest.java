package com.github.jothammicheni.daraja.dto.webhook;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookPayloadTest {

    @Test
    void shouldBuildPayload() {
        WebhookPayload payload = WebhookPayload.builder()
                .merchantRequestId("MER-123")
                .checkoutRequestId("CHECK-456")
                .resultCode("0")
                .resultDescription("Success")
                .transactionId("TXN-789")
                .amount("100.00")
                .phoneNumber("254712345678")
                .accountReference("ORDER-999")
                .receiptNumber("RCPT-123")
                .build();

        assertThat(payload.getMerchantRequestId()).isEqualTo("MER-123");
        assertThat(payload.getCheckoutRequestId()).isEqualTo("CHECK-456");
        assertThat(payload.getResultCode()).isEqualTo("0");
        assertThat(payload.getResultDescription()).isEqualTo("Success");
        assertThat(payload.getTransactionId()).isEqualTo("TXN-789");
        assertThat(payload.getAmount()).isEqualTo("100.00");
        assertThat(payload.getPhoneNumber()).isEqualTo("254712345678");
        assertThat(payload.getMaskedPhoneNumber()).isEqualTo("******5678");
        assertThat(payload.getAccountReference()).isEqualTo("ORDER-999");
        assertThat(payload.getReceiptNumber()).isEqualTo("RCPT-123");
        assertThat(payload.getReceivedAt()).isNotNull();
        assertThat(payload.isSuccess()).isTrue();
        assertThat(payload.isFailed()).isFalse();
    }

    @Test
    void shouldDetectFailedPayment() {
        WebhookPayload payload = WebhookPayload.builder()
                .resultCode("1032")
                .resultDescription("User cancelled")
                .build();

        assertThat(payload.isSuccess()).isFalse();
        assertThat(payload.isFailed()).isTrue();
        assertThat(payload.getSummary()).contains("FAILED");
    }

    @Test
    void shouldMaskPhoneNumber() {
        WebhookPayload payload = WebhookPayload.builder()
                .phoneNumber("254712345678")
                .build();
        assertThat(payload.getMaskedPhoneNumber()).isEqualTo("******5678");

        // Null phone
        WebhookPayload payload2 = WebhookPayload.builder().build();
        assertThat(payload2.getMaskedPhoneNumber()).isNull();
    }

    @Test
    void shouldSetReceivedAtAutomatically() {
        WebhookPayload payload = WebhookPayload.builder().build();
        assertThat(payload.getReceivedAt()).isNotNull();
    }

    @Test
    void shouldHaveToString() {
        WebhookPayload payload = WebhookPayload.builder()
                .transactionId("TXN-123")
                .amount("50.00")
                .build();
        assertThat(payload.toString()).contains("transactionId='TXN-123'")
                .contains("amount='50.00'");
    }
}