package com.github.jothammicheni.daraja_springboot_starter_jdk.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "mpesa.daraja") // ⚡ ONLY annotation at the class level
public record MpesaProperties(
        @NotBlank String consumerKey,
        @NotBlank String consumerSecret,
        @NotBlank String apiSecret,
        MpesaEnvironment environment,
        String baseUrl,
        int connectTimeout,
        int readTimeout,
        String appUrl,
        String callbackUrl,
        String cacheType,
        String confirmationUrlPath,
        String validationUrlPath,
        boolean enableIpValidation,
        boolean isBehindProxy
) {
    // ⚡ FIXED: Removed @ConstructorBinding from here completely
    public MpesaProperties {
        environment = environment != null ? environment : MpesaEnvironment.SANDBOX;

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = (environment == MpesaEnvironment.PRODUCTION)
                    ? "https://safaricom.co.ke"
                    : "https://safaricom.co.ke";
        }

        connectTimeout = connectTimeout > 0 ? connectTimeout : 10;
        readTimeout = readTimeout > 0 ? readTimeout : 30;

        confirmationUrlPath = confirmationUrlPath != null ? confirmationUrlPath : "/cb/confirmation";
        validationUrlPath = validationUrlPath != null ? validationUrlPath : "/cb/validation";
        cacheType = cacheType != null ? cacheType : "local";

        appUrl = appUrl != null ? appUrl : "http://localhost:8080";
        if (callbackUrl == null || callbackUrl.isBlank()) {
            String cleanAppUrl = appUrl.endsWith("/") ? appUrl.substring(0, appUrl.length() - 1) : appUrl;
            String cleanPath = confirmationUrlPath.startsWith("/") ? confirmationUrlPath : "/" + confirmationUrlPath;
            callbackUrl = cleanAppUrl + cleanPath;
        }
    }
}
