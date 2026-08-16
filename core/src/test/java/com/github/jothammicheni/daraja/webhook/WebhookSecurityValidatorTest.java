package com.github.jothammicheni.daraja.webhook;

import com.github.jothammicheni.daraja.config.MpesaConfig;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class WebhookSecurityValidatorTest {

    @Test
    void shouldReturnTrueWhenIpValidationIsDisabled() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .enableIpValidation(false)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        boolean result = validator.isAuthenticRequest("1.2.3.4");

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueForSafaricomProductionIp() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .enableIpValidation(true)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        boolean result = validator.isAuthenticRequest("196.201.214.100");

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueForSafaricomSandboxIp() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .enableIpValidation(true)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        boolean result = validator.isAuthenticRequest("196.201.212.50");

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForNonSafaricomIp() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .enableIpValidation(true)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        boolean result = validator.isAuthenticRequest("8.8.8.8");

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseForInvalidIp() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .enableIpValidation(true)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        boolean result = validator.isAuthenticRequest(null);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseForEmptyIp() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .enableIpValidation(true)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        boolean result = validator.isAuthenticRequest("");

        assertThat(result).isFalse();
    }

    @Test
    void shouldResolveClientIpFromXForwardedFor() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .isBehindProxy(true)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        String ip = validator.resolveClientIp(
            "196.201.214.100, 10.0.0.1",
            null,
            null,
            null,
            "127.0.0.1"
        );

        assertThat(ip).isEqualTo("196.201.214.100");
    }

    @Test
    void shouldResolveClientIpFromXRealIp() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .isBehindProxy(true)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        String ip = validator.resolveClientIp(
            null,
            "196.201.214.100",
            null,
            null,
            "127.0.0.1"
        );

        assertThat(ip).isEqualTo("196.201.214.100");
    }

    @Test
    void shouldResolveClientIpFromRemoteAddrWhenNoProxyHeaders() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .isBehindProxy(true)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        String ip = validator.resolveClientIp(
            null,
            null,
            null,
            null,
            "196.201.214.100"
        );

        assertThat(ip).isEqualTo("196.201.214.100");
    }

    @Test
    void shouldCleanIPv6LoopbackAddress() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .enableIpValidation(true)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        boolean result = validator.isAuthenticRequest("0:0:0:0:0:0:0:1");

        assertThat(result).isFalse();
    }

    @Test
    void shouldSkipProxyResolutionWhenNotBehindProxy() {
        MpesaConfig config = new MpesaConfig.Builder(
            "consumer-key",
            "consumer-secret",
            "api-secret"
        )
        .isBehindProxy(false)
        .build();

        WebhookSecurityValidator validator = new WebhookSecurityValidator(config);
        String ip = validator.resolveClientIp(
            "196.201.214.100",
            null,
            null,
            null,
            "127.0.0.1"
        );

        assertThat(ip).isEqualTo("127.0.0.1");
    }
}