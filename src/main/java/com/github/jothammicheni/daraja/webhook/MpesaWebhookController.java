package com.github.jothammicheni.daraja_springboot_starter_jdk.core.webhook;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MpesaWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MpesaWebhookController.class);

    // ⚡ FIXED: Switched to explicit field injection to stop 2-bean lookup crashes on third-party integration boots
    @Autowired
    private WebhookSecurityValidator securityValidator;

    // Zero-argument constructor preserves standalone invocation compatibility across all compiler flavors
    public MpesaWebhookController() {
    }

    // ⚡ DYNAMIC PATH: Resolves to properties file value at application launch
    @PostMapping("${mpesa.daraja.confirmation-url-path:/cb/confirmation}")
    public ResponseEntity<Map<String, String>> handleConfirmation(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            HttpServletRequest request) {

        MDC.put("correlationId", correlationId != null ? correlationId : "unknown");
        MDC.put("webhookType", "confirmation");

        try {
            // 🛡️ Guard Clause: Blocks fake network traffic using real Safaricom IP subnets
            if (!securityValidator.isAuthenticRequest(request)) {
                log.error("Unauthorized webhook confirmation attempt - Invalid network origin.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("ResultCode", "1", "ResultDesc", "Access denied: Unauthorized source"));
            }

            log.info("Webhook received successfully from Safaricom. Body length: {} bytes", rawBody.length());

            // Standard JSON structure expected by Safaricom's Daraja gateway engine
            return ResponseEntity.ok(Map.of("ResultCode", "0", "ResultDesc", "Success"));
        } finally {
            MDC.clear();
        }
    }

    // ⚡ DYNAMIC PATH: Falls back to default path if the importing app doesn't specify one
    @PostMapping("${mpesa.daraja.validation-url-path:/cb/validation}")
    public ResponseEntity<Map<String, String>> handleValidation(
            @RequestBody String rawBody,
            HttpServletRequest request) {

        // 🛡️ Guard Clause: Secure the validation route block
        if (!securityValidator.isAuthenticRequest(request)) {
            log.error("Unauthorized webhook validation attempt - Invalid network origin.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ResultCode", "1", "ResultDesc", "Validation rejected"));
        }

        log.info("Safaricom Validation passed network filter checks. Body length: {} bytes", rawBody.length());

        return ResponseEntity.ok(Map.of("ResultCode", "0", "ResultDesc", "Validation passed"));
    }
}
