package com.github.jothammicheni.daraja.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MpesaConfigTest {

    @Test
    void shouldCreateConfigWithRequiredFields() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        ).build();

        assertThat(config.getConsumerKey()).isEqualTo("consumer-key");
        assertThat(config.getConsumerSecret()).isEqualTo("consumer-secret");
        assertThat(config.getApiSecret()).isEqualTo("api-secret");
        assertThat(config.getEnvironment()).isEqualTo(MpesaEnvironment.SANDBOX);
        assertThat(config.getBaseUrl()).isEqualTo("https://sandbox.safaricom.co.ke");
        assertThat(config.getConnectTimeout()).isEqualTo(10);
        assertThat(config.getReadTimeout()).isEqualTo(30);
        assertThat(config.getAppUrl()).isEqualTo("http://localhost:8080");
        assertThat(config.getCacheType()).isEqualTo("local");
        assertThat(config.getConfirmationUrlPath()).isEqualTo("/cb/confirmation");
        assertThat(config.getValidationUrlPath()).isEqualTo("/cb/validation");
        assertThat(config.isEnableIpValidation()).isFalse();
        assertThat(config.isBehindProxy()).isFalse();
    }

    @Test
    void shouldUseProductionUrlWhenEnvironmentIsProduction() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .environment(MpesaEnvironment.PRODUCTION)
        .build();

        assertThat(config.getBaseUrl()).isEqualTo("https://safaricom.co.ke");
    }

    @Test
    void shouldUseCustomBaseUrlWhenProvided() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .baseUrl("https://custom.api.com")
        .build();

        assertThat(config.getBaseUrl()).isEqualTo("https://custom.api.com");
    }

    @Test
    void shouldBuildCallbackUrlFromAppUrlAndConfirmationPath() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .appUrl("https://myapp.com")
        .confirmationUrlPath("/mpesa/callback")
        .build();

        assertThat(config.getCallbackUrl()).isEqualTo("https://myapp.com/mpesa/callback");
    }

    @Test
    void shouldUseCustomCallbackUrlWhenProvided() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .callbackUrl("https://custom.com/webhook")
        .build();

        assertThat(config.getCallbackUrl()).isEqualTo("https://custom.com/webhook");
    }

    @Test
    void shouldUseCustomTimeouts() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .connectTimeout(20)
        .readTimeout(60)
        .build();

        assertThat(config.getConnectTimeout()).isEqualTo(20);
        assertThat(config.getReadTimeout()).isEqualTo(60);
    }

    @Test
    void shouldEnableIpValidationAndProxy() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .enableIpValidation(true)
        .isBehindProxy(true)
        .build();

        assertThat(config.isEnableIpValidation()).isTrue();
        assertThat(config.isBehindProxy()).isTrue();
    }

    @Test
    void shouldUseCustomCacheType() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .cacheType("redis")
        .build();

        assertThat(config.getCacheType()).isEqualTo("redis");
    }
}