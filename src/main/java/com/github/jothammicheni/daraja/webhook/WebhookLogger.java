package com.github.jothammicheni.daraja.webhook;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Pure Java webhook logger - logs only essential information.
 */
public class WebhookLogger {

    private static final Logger log = Logger.getLogger(WebhookLogger.class.getName());

    /**
     * Log only the important webhook data.
     */
    public static void logWebhook(Map<String, Object> rawData, String webhookType) {
        try {
            // Extract from Body.stkCallback
            Map<String, Object> body = (Map<String, Object>) rawData.get("Body");
            Map<String, Object> stkCallback = body != null ? (Map<String, Object>) body.get("stkCallback") : rawData;

            if (stkCallback == null) {
                log.info("📨 Webhook received but no stkCallback found");
                return;
            }

            String resultCode = getString(stkCallback, "ResultCode");
            String resultDesc = getString(stkCallback, "ResultDesc");
            String checkoutId = getString(stkCallback, "CheckoutRequestID");
            String merchantId = getString(stkCallback, "MerchantRequestID");
            String receipt = getString(stkCallback, "ReceiptNumber");
            String amount = getString(stkCallback, "TransAmount");
            String phone = getString(stkCallback, "PhoneNumber");

            // ✅ Essential log only
            StringBuilder logEntry = new StringBuilder();
            logEntry.append("\n📨 ").append(webhookType).append(" WEBHOOK\n");
            logEntry.append("   Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
            logEntry.append("   Status: ").append("0".equals(resultCode) ? "✅ SUCCESS" : "❌ FAILED").append("\n");
            logEntry.append("   Checkout ID: ").append(checkoutId != null ? checkoutId : "N/A").append("\n");
            logEntry.append("   Result: ").append(resultCode != null ? resultCode : "N/A").append(" - ").append(resultDesc != null ? resultDesc : "N/A").append("\n");

            if (amount != null) logEntry.append("   Amount: ").append(amount).append("\n");
            if (receipt != null) logEntry.append("   Receipt: ").append(receipt).append("\n");
            if (phone != null) logEntry.append("   Phone: ").append(maskPhoneNumber(phone)).append("\n");
            if (merchantId != null) logEntry.append("   Merchant ID: ").append(merchantId).append("\n");

            log.info(logEntry.toString());

        } catch (Exception e) {
            log.warning("Failed to log webhook: " + e.getMessage());
        }
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        String cleaned = phone.replaceAll("[^\\d]", "");
        if (cleaned.length() < 4) return "****";
        return "******" + cleaned.substring(cleaned.length() - 4);
    }
}