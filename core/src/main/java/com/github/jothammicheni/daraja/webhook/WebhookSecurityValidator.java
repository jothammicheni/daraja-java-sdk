package com.github.jothammicheni.daraja.webhook;

import com.github.jothammicheni.daraja.config.MpesaConfig;
import java.util.List;

public class WebhookSecurityValidator {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(WebhookSecurityValidator.class.getName());

    // Official Safaricom Daraja Production and Sandbox Gateway IP ranges
    private static final List<String> SAFARICOM_IP_PREFIXES = List.of(
            "196.201.214.", // Production Gateway 1
            "196.201.213.", // Production Gateway 2
            "196.201.212.", // Sandbox Gateway
            "196.22.131."   // Additional Safaricom Cloud infrastructure blocks
    );

    private final MpesaConfig config;

    public WebhookSecurityValidator(MpesaConfig config) {
        this.config = config;
    }

    /**
     * Authenticates whether the incoming traffic source matches trusted Safaricom addresses.
     */
    public boolean isAuthenticRequest(String clientIp) {
        if (!config.isEnableIpValidation()) {
            log.fine("Skipping Webhook IP source validation as configured");
            return true;
        }

        if (clientIp == null || clientIp.isBlank()) {
            log.severe("🚨 Webhook authentication aborted. Client source IP could not be resolved.");
            return false;
        }

        // Clean IPv6 loopback addresses
        if (clientIp.equals("0:0:0:0:0:0:0:1")) {
            clientIp = "127.0.0.1";
        }

        boolean matchesSafaricom = SAFARICOM_IP_PREFIXES.stream().anyMatch(clientIp::startsWith);

        if (!matchesSafaricom) {
            log.warning("🚨 Unauthorized webhook source blocked. Request rejected from source IP: " + clientIp);
            return false;
        }

        log.fine("🔒 Webhook request origin authenticated successfully for IP: " + clientIp);
        return true;
    }

    /**
     * Resolves the actual origin client IP address from proxy headers.
     */
    public String resolveClientIp(String xForwardedFor, String xRealIp, String cfConnectingIp, String trueClientIp, String remoteAddr) {
        if (config.isBehindProxy()) {
            String[] headersToInspect = {xForwardedFor, xRealIp, cfConnectingIp, trueClientIp};

            for (String headerValue : headersToInspect) {
                if (headerValue != null && !headerValue.isBlank()) {
                    String[] proxyChain = headerValue.split(",");
                    String originalClient = proxyChain[0].trim();
                    if (!originalClient.equalsIgnoreCase("unknown")) {
                        return originalClient;
                    }
                }
            }
        }
        return remoteAddr;
    }
}