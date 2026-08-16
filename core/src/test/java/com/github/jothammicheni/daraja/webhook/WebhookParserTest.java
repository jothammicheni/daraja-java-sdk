package com.github.jothammicheni.daraja.webhook;

import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookParserTest {

    @Test
    void shouldParseStkPushSuccessCallback() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> stkCallback = new HashMap<>();
        stkCallback.put("MerchantRequestID", "MER-123");
        stkCallback.put("CheckoutRequestID", "CHECK-456");
        stkCallback.put("ResultCode", "0");
        stkCallback.put("ResultDesc", "Success");
        Map<String, Object> metadata = new HashMap<>();
        Map<String, Object> item1 = Map.of("Name", "Amount", "Value", 100.0);
        Map<String, Object> item2 = Map.of("Name", "MpesaReceiptNumber", "Value", "RCPT-789");
        Map<String, Object> item3 = Map.of("Name", "PhoneNumber", "Value", 254712345678L);
        metadata.put("Item", List.of(item1, item2, item3));
        stkCallback.put("CallbackMetadata", metadata);
        body.put("stkCallback", stkCallback);
        rawData.put("Body", body);

        WebhookPayload payload = WebhookParser.parseWebhook(rawData);

        assertThat(payload.getMerchantRequestId()).isEqualTo("MER-123");
        assertThat(payload.getCheckoutRequestId()).isEqualTo("CHECK-456");
        assertThat(payload.getResultCode()).isEqualTo("0");
        assertThat(payload.getResultDescription()).isEqualTo("Success");
        assertThat(payload.getAmount()).isEqualTo("100.0");
        assertThat(payload.getReceiptNumber()).isEqualTo("RCPT-789");
        assertThat(payload.getTransactionId()).isEqualTo("RCPT-789");
        assertThat(payload.getPhoneNumber()).isEqualTo("254712345678");
        assertThat(payload.isSuccess()).isTrue();
    }

    @Test
    void shouldParseStkPushCancelledCallback() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> stkCallback = new HashMap<>();
        stkCallback.put("MerchantRequestID", "MER-123");
        stkCallback.put("CheckoutRequestID", "CHECK-456");
        stkCallback.put("ResultCode", "1032");
        stkCallback.put("ResultDesc", "User cancelled");
        body.put("stkCallback", stkCallback);
        rawData.put("Body", body);

        WebhookPayload payload = WebhookParser.parseWebhook(rawData);

        assertThat(payload.getResultCode()).isEqualTo("1032");
        assertThat(payload.getResultDescription()).isEqualTo("User cancelled");
        assertThat(payload.getAmount()).isNull();
        assertThat(payload.getReceiptNumber()).isNull();
        assertThat(payload.isSuccess()).isFalse();
    }

    @Test
    void shouldParseFlatC2BCallback() {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("TransID", "TXN-123");
        rawData.put("TransAmount", "200.00");
        rawData.put("MSISDN", "254712345678");
        rawData.put("AccountReference", "ORDER-999");
        rawData.put("ResultCode", "0");
        rawData.put("ResultDesc", "Success");

        WebhookPayload payload = WebhookParser.parseWebhook(rawData);

        assertThat(payload.getTransactionId()).isEqualTo("TXN-123");
        assertThat(payload.getAmount()).isEqualTo("200.00");
        assertThat(payload.getPhoneNumber()).isEqualTo("254712345678");
        assertThat(payload.getAccountReference()).isEqualTo("ORDER-999");
        assertThat(payload.isSuccess()).isTrue();
    }

    @Test
    void shouldParseWhenNoBody() {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("ResultCode", "0");

        WebhookPayload payload = WebhookParser.parseWebhook(rawData);

        assertThat(payload.getResultCode()).isEqualTo("0");
    }

    @Test
    void shouldHandleMissingCallbackMetadata() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> stkCallback = new HashMap<>();
        stkCallback.put("ResultCode", "0");
        body.put("stkCallback", stkCallback);
        rawData.put("Body", body);

        WebhookPayload payload = WebhookParser.parseWebhook(rawData);

        assertThat(payload.isSuccess()).isTrue();
        assertThat(payload.getAmount()).isNull();
    }

    @Test
    void shouldHandleEmptyMetadataItemList() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> stkCallback = new HashMap<>();
        stkCallback.put("ResultCode", "0");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("Item", List.of());
        stkCallback.put("CallbackMetadata", metadata);
        body.put("stkCallback", stkCallback);
        rawData.put("Body", body);

        WebhookPayload payload = WebhookParser.parseWebhook(rawData);

        assertThat(payload.isSuccess()).isTrue();
        assertThat(payload.getAmount()).isNull();
    }

    @Test
    void shouldHandleMalformedMetadataItem() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> stkCallback = new HashMap<>();
        stkCallback.put("ResultCode", "0");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("Item", List.of("not a map"));
        stkCallback.put("CallbackMetadata", metadata);
        body.put("stkCallback", stkCallback);
        rawData.put("Body", body);

        WebhookPayload payload = WebhookParser.parseWebhook(rawData);

        assertThat(payload.isSuccess()).isTrue();
        assertThat(payload.getAmount()).isNull();
    }

    @Test
    void shouldPreserveExistingFieldsWhenMetadataHasNullValue() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> stkCallback = new HashMap<>();
        stkCallback.put("ResultCode", "0");
        stkCallback.put("TransAmount", "50.00");

        Map<String, Object> metadata = new HashMap<>();
        Map<String, Object> item = new HashMap<>();
        item.put("Name", "Amount");
        item.put("Value", null);
        metadata.put("Item", List.of(item));
        stkCallback.put("CallbackMetadata", metadata);

        body.put("stkCallback", stkCallback);
        rawData.put("Body", body);

        WebhookPayload payload = WebhookParser.parseWebhook(rawData);

        assertThat(payload.getAmount()).isEqualTo("50.00");
    }
}