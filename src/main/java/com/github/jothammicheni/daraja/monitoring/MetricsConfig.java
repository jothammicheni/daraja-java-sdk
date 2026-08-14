package com.github.jothammicheni.daraja.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;


/**
 * Enterprise-grade metrics configuration for the SDK.
 * No dependencies on Spring - pure Java!
 */
public final class MetricsConfig {

    private static final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    private static volatile boolean initialized = false;

    // ✅ Core business metrics
    private static Counter stkPushTotal;
    private static Counter stkPushSuccess;
    private static Counter stkPushFailure;
    private static Timer stkPushTimer;
    private static DistributionSummary stkPushLatency;

    // ✅ OAuth metrics
    private static Counter tokenRefreshTotal;
    private static Counter tokenRefreshSuccess;
    private static Timer tokenRefreshTimer;

    // ✅ Webhook metrics
    private static Counter webhookReceived;
    private static Counter webhookSuccess;
    private static Counter webhookFailure;
    private static Timer webhookTimer;

    // ✅ Cache metrics
    private static Counter cacheHit;
    private static Counter cacheMiss;

    static {
        initialize();
    }

    private static void initialize() {
        if (initialized) return;

        // STK Push Metrics
        stkPushTotal = Counter.builder("mpesa.stkpush.total")
                .description("Total STK Push requests")
                .register(registry);

        stkPushSuccess = Counter.builder("mpesa.stkpush.success")
                .description("Successful STK Push requests")
                .register(registry);

        stkPushFailure = Counter.builder("mpesa.stkpush.failure")
                .description("Failed STK Push requests")
                .register(registry);

        stkPushTimer = Timer.builder("mpesa.stkpush.duration")
                .description("STK Push request duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        stkPushLatency = DistributionSummary.builder("mpesa.stkpush.latency")
                .description("STK Push latency distribution")
                .baseUnit("milliseconds")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        // OAuth Metrics
        tokenRefreshTotal = Counter.builder("mpesa.oauth.total")
                .description("Total OAuth token refreshes")
                .register(registry);

        tokenRefreshSuccess = Counter.builder("mpesa.oauth.success")
                .description("Successful OAuth token refreshes")
                .register(registry);

        tokenRefreshTimer = Timer.builder("mpesa.oauth.duration")
                .description("OAuth token refresh duration")
                .register(registry);

        // Webhook Metrics
        webhookReceived = Counter.builder("mpesa.webhook.received")
                .description("Total webhooks received")
                .register(registry);

        webhookSuccess = Counter.builder("mpesa.webhook.success")
                .description("Successful webhook processing")
                .register(registry);

        webhookFailure = Counter.builder("mpesa.webhook.failure")
                .description("Failed webhook processing")
                .register(registry);

        webhookTimer = Timer.builder("mpesa.webhook.duration")
                .description("Webhook processing duration")
                .register(registry);

        // Cache Metrics
        cacheHit = Counter.builder("mpesa.cache.hit")
                .description("Cache hits")
                .register(registry);

        cacheMiss = Counter.builder("mpesa.cache.miss")
                .description("Cache misses")
                .register(registry);

        initialized = true;
    }

    // ✅ Getters for metrics recording

    public static Counter getStkPushTotal() { return stkPushTotal; }
    public static Counter getStkPushSuccess() { return stkPushSuccess; }
    public static Counter getStkPushFailure() { return stkPushFailure; }
    public static Timer getStkPushTimer() { return stkPushTimer; }
    public static DistributionSummary getStkPushLatency() { return stkPushLatency; }

    public static Counter getTokenRefreshTotal() { return tokenRefreshTotal; }
    public static Counter getTokenRefreshSuccess() { return tokenRefreshSuccess; }
    public static Timer getTokenRefreshTimer() { return tokenRefreshTimer; }

    public static Counter getWebhookReceived() { return webhookReceived; }
    public static Counter getWebhookSuccess() { return webhookSuccess; }
    public static Counter getWebhookFailure() { return webhookFailure; }
    public static Timer getWebhookTimer() { return webhookTimer; }

    public static Counter getCacheHit() { return cacheHit; }
    public static Counter getCacheMiss() { return cacheMiss; }

    /**
     * Get the Prometheus registry for exposing metrics.
     */
    public static PrometheusMeterRegistry getRegistry() {
        return registry;
    }

    /**
     * Scrape metrics for Prometheus.
     */
    public static String scrape() {
        return registry.scrape();
    }
}