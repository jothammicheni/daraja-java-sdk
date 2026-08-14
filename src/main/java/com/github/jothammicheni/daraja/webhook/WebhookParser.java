package com.github.jothammicheni.daraja.webhook;

import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;
import com.github.jothammicheni.daraja.dto.webhook.WebhookResponse;

import java.util.List;
import java.util.Map;

/**
 * Pure Java webhook parser.
 * No Spring dependencies.
 *
 * Handles Safaricom's STK Push callback shape, which nests everything under
 * Body.stkCallback and only includes CallbackMetadata.Item on success
 * (ResultCode == 0). Cancelled/failed callbacks (e.g. 1032, 1037) will
 * legitimately have null amount/receipt/transactionId/phoneNumber — that's
 * expected, not a parsing failure.
 */
public class WebhookParser {

    /**
     * Parse raw webhook data into structured WebhookPayload.
     */
    @SuppressWarnings("unchecked")
    public static WebhookPayload parseWebhook(Map<String, Object> rawData) {
        Map<String, Object> stkCallback = unwrapStkCallback(rawData);

        // If it's not an STK callback shape (e.g. a C2B confirmation/validation
        // webhook, which IS flat), fall back to treating rawData as flat.
        Map<String, Object> source = (stkCallback != null) ? stkCallback : rawData;

        Map<String, Object> metadata = extractCallbackMetadata(source);

        WebhookPayload.Builder builder = WebhookPayload.builder()
                .merchantRequestId(getString(source, "MerchantRequestID"))
                .checkoutRequestId(getString(source, "CheckoutRequestID"))
                .resultCode(getString(source, "ResultCode"))
                .resultDescription(getString(source, "ResultDesc"))
                // C2B-style flat fields (used when this isn't an STK callback)
                .transactionId(getString(source, "TransID"))
                .transactionDate(getString(source, "TransTime"))
                .amount(getString(source, "TransAmount"))
                .phoneNumber(getString(source, "PhoneNumber"))
                .accountReference(getString(source, "AccountReference"))
                .receiptNumber(getString(source, "ReceiptNumber"))
                .customerMessage(getString(source, "CustomerMessage"));

        // Overlay STK Push CallbackMetadata values (success only) if present.
        // These take priority since they're the actual source of truth for
        // STK Push flows, and only exist when the payment succeeded.
        if (metadata != null) {
            String amount = getMetadataValue(metadata, "Amount");
            String receipt = getMetadataValue(metadata, "MpesaReceiptNumber");
            String transDate = getMetadataValue(metadata, "TransactionDate");
            String phone = getMetadataValue(metadata, "PhoneNumber");

            if (amount != null) builder.amount(amount);
            if (receipt != null) {
                builder.receiptNumber(receipt);
                // M-Pesa receipt number doubles as the transaction identifier
                // for STK Push flows (there's no separate TransID field there).
                builder.transactionId(receipt);
            }
            if (transDate != null) builder.transactionDate(transDate);
            if (phone != null) builder.phoneNumber(phone);
        }

        return builder.build();
    }

    /**
     * Unwraps Body.stkCallback if present. Returns null if the payload
     * doesn't match the STK Push callback shape (e.g. it's a flat C2B webhook).
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> unwrapStkCallback(Map<String, Object> rawData) {
        Object body = rawData.get("Body");
        if (!(body instanceof Map)) {
            return null;
        }
        Object stkCallback = ((Map<String, Object>) body).get("stkCallback");
        if (!(stkCallback instanceof Map)) {
            return null;
        }
        return (Map<String, Object>) stkCallback;
    }

    /**
     * Extracts CallbackMetadata.Item as a Name -> Value map, if present.
     * Returns null on failed/cancelled callbacks, which omit CallbackMetadata.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractCallbackMetadata(Map<String, Object> stkCallback) {
        Object metadataObj = stkCallback.get("CallbackMetadata");
        if (!(metadataObj instanceof Map)) {
            return null;
        }
        Object itemsObj = ((Map<String, Object>) metadataObj).get("Item");
        if (!(itemsObj instanceof List)) {
            return null;
        }

        List<Object> items = (List<Object>) itemsObj;
        Map<String, Object> result = new java.util.HashMap<>();
        for (Object itemObj : items) {
            if (itemObj instanceof Map) {
                Map<String, Object> item = (Map<String, Object>) itemObj;
                Object name = item.get("Name");
                if (name != null) {
                    result.put(name.toString(), item.get("Value"));
                }
            }
        }
        return result;
    }

    private static String getMetadataValue(Map<String, Object> metadata, String name) {
        Object value = metadata.get(name);
        return value != null ? value.toString() : null;
    }

    /**
     * Create a success response.
     */
    public static Map<String, String> successResponse() {
        return WebhookResponse.success().toMap();
    }

    /**
     * Create a failure response.
     */
    public static Map<String, String> failureResponse(String message) {
        return WebhookResponse.failure(message).toMap();
    }

    /**
     * Create a failure response with custom code.
     */
    public static Map<String, String> failureResponse(String code, String message) {
        return WebhookResponse.failure(code, message).toMap();
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}