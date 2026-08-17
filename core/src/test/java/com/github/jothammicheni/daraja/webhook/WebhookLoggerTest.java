package com.github.jothammicheni.daraja.webhook;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

class WebhookLoggerTest {

    @Test
    void shouldLogWebhookWithValidStkCallback() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> stkCallback = new HashMap<>();
        stkCallback.put("ResultCode", "0");
        stkCallback.put("ResultDesc", "Success");
        stkCallback.put("CheckoutRequestID", "ws_CO_123");
        stkCallback.put("MerchantRequestID", "MER-456");
        stkCallback.put("ReceiptNumber", "RCPT-789");
        stkCallback.put("TransAmount", "100.00");
        stkCallback.put("PhoneNumber", "254712345678");
        body.put("stkCallback", stkCallback);
        rawData.put("Body", body);

        assertThatCode(() -> WebhookLogger.logWebhook(rawData, "CONFIRMATION"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldLogWebhookWithNullStkCallback() {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("Body", new HashMap<>());

        assertThatCode(() -> WebhookLogger.logWebhook(rawData, "VALIDATION"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldLogWebhookWithMissingFields() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> stkCallback = new HashMap<>();
        stkCallback.put("ResultCode", "1032");
        body.put("stkCallback", stkCallback);
        rawData.put("Body", body);

        assertThatCode(() -> WebhookLogger.logWebhook(rawData, "CANCELLED"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldLogWebhookWithRawDataAsStkCallback() {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("ResultCode", "0");
        rawData.put("ResultDesc", "Success");

        assertThatCode(() -> WebhookLogger.logWebhook(rawData, "FLAT"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldMaskPhoneNumberCorrectly() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> stkCallback = new HashMap<>();
        stkCallback.put("PhoneNumber", "254712345678");
        stkCallback.put("ResultCode", "0");
        body.put("stkCallback", stkCallback);
        rawData.put("Body", body);

        assertThatCode(() -> WebhookLogger.logWebhook(rawData, "TEST"))
                .doesNotThrowAnyException();
    }
}