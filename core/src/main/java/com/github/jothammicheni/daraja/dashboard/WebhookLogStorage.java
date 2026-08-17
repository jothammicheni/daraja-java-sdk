package com.github.jothammicheni.daraja.dashboard;

import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory storage for webhook logs.
 * Thread-safe, stores last 1000 logs.
 * Pure Java - no framework dependencies.
 */
public class WebhookLogStorage {

    private static final ConcurrentHashMap<String, WebhookLogEntry> logs = new ConcurrentHashMap<>();
    private static final int MAX_LOGS = 1000;

    /**
     * Store a webhook log entry.
     */
    public static void store(WebhookPayload payload, String rawJson) {
        String id = payload.getCheckoutRequestId() != null
                ? payload.getCheckoutRequestId()
                : "webhook-" + System.currentTimeMillis();

        String status = payload.isSuccess() ? "SUCCESS" :
                "1032".equals(payload.getResultCode()) ? "CANCELLED" :
                        "FAILED";

        WebhookLogEntry entry = new WebhookLogEntry(
                id,
                Instant.now(),
                status,
                payload.getResultCode(),
                payload.getResultDescription(),
                payload.getAmount(),
                payload.getPhoneNumber(),
                payload.getMaskedPhoneNumber(),
                payload.getReceiptNumber(),
                payload.getCheckoutRequestId(),
                payload.getMerchantRequestId(),
                payload.getAccountReference(),
                rawJson
        );

        // Remove oldest entry if at capacity
        if (logs.size() >= MAX_LOGS) {
            String oldestKey = logs.keys().nextElement();
            logs.remove(oldestKey);
        }

        logs.put(id, entry);
    }

    /**
     * Get recent logs with limit.
     */
    public static List<WebhookLogEntry> getRecentLogs(int limit) {
        return logs.values().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get logs filtered by status.
     */
    public static List<WebhookLogEntry> getLogsByStatus(String status) {
        return logs.values().stream()
                .filter(entry -> entry.getStatus().equals(status))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get all logs.
     */
    public static List<WebhookLogEntry> getAllLogs() {
        return new ArrayList<>(logs.values());
    }

    /**
     * Get statistics.
     */
    public static Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", logs.size());
        stats.put("success", (int) logs.values().stream().filter(e -> "SUCCESS".equals(e.getStatus())).count());
        stats.put("failed", (int) logs.values().stream().filter(e -> "FAILED".equals(e.getStatus())).count());
        stats.put("cancelled", (int) logs.values().stream().filter(e -> "CANCELLED".equals(e.getStatus())).count());
        return stats;
    }

    /**
     * Clear all logs.
     */
    public static void clearLogs() {
        logs.clear();
    }

    /**
     * Get total count.
     */
    public static int getCount() {
        return logs.size();
    }

    /**
     * Check if there are any logs.
     */
    public static boolean isEmpty() {
        return logs.isEmpty();
    }
}