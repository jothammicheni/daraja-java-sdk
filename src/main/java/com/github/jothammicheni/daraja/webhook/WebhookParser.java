package com.github.jothammicheni.daraja.webhook;

import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;
import com.github.jothammicheni.daraja.dto.webhook.WebhookResponse;

import java.util.Map;

/**
 * Pure Java webhook parser.
 * No Spring dependencies.
 */
public class WebhookParser {

    /**
     * Parse raw webhook data into structured WebhookPayload.
     */
    public static WebhookPayload parseWebhook(Map<String, Object> rawData) {
        return WebhookPayload.builder()
                .merchantRequestId(getString(rawData, "MerchantRequestID"))
                .checkoutRequestId(getString(rawData, "CheckoutRequestID"))
                .resultCode(getString(rawData, "ResultCode"))
                .resultDescription(getString(rawData, "ResultDesc"))
                .transactionId(getString(rawData, "TransID"))
                .transactionDate(getString(rawData, "TransTime"))
                .amount(getString(rawData, "TransAmount"))
                .phoneNumber(getString(rawData, "PhoneNumber"))
                .accountReference(getString(rawData, "AccountReference"))
                .receiptNumber(getString(rawData, "ReceiptNumber"))
                .customerMessage(getString(rawData, "CustomerMessage"))
                .build();
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