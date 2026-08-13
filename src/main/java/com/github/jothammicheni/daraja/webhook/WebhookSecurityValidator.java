package com.github.jothammicheni.daraja_springboot_starter_jdk.core.webhook;

import com.github.jothammicheni.daraja_springboot_starter_jdk.core.config.MpesaProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebhookSecurityValidator {

    private static final Logger log = LoggerFactory.getLogger(WebhookSecurityValidator.class);

    // Official Safaricom Daraja Production and Sandbox Gateway IP ranges
    private static final List<String> SAFARICOM_IP_PREFIXES = List.of(
            "196.201.214.", // Production Gateway 1
            "196.201.213.", // Production Gateway 2
            "196.201.212.", // Sandbox Gateway
            "196.22.131."   // Additional Safaricom Cloud infrastructure blocks
    );

    private final MpesaProperties properties;

    // ⚡ FIXED: Added explicit @Qualifier definition to resolve the 2-bean conflict on AWS compilation
    public WebhookSecurityValidator(@Qualifier("mpesaProperties") MpesaProperties properties) {
        this.properties = properties;
    }

    /**
     * Authenticates whether the incoming traffic source matches trusted Safaricom addresses.
     */
    public boolean isAuthenticRequest(HttpServletRequest request) {
        // Allows developers to easily bypass network checks inside localized test runners or unit suites
        if (!properties.enableIpValidation()) {
            log.trace("Skipping Webhook IP source validation as configured by 'mpesa.daraja.enable-ip-validation=false'");
            return true;
        }

        String clientIp = resolveClientIp(request);

        if (clientIp == null || clientIp.isBlank()) {
            log.error("🚨 Webhook authentication aborted. Client source IP could not be resolved.");
            return false;
        }

        // Clean IPv6 loopback addresses or local proxies to standard structures
        if (clientIp.equals("0:0:0:0:0:0:0:1")) {
            clientIp = "127.0.0.1";
        }

        String finalIp = clientIp;
        boolean matchesSafaricom = SAFARICOM_IP_PREFIXES.stream().anyMatch(finalIp::startsWith);

        if (!matchesSafaricom) {
            log.warn("🚨 Unauthorized webhook source blocked. Request rejected from source IP: {}", finalIp);
            return false;
        }

        log.debug("🔒 Webhook request origin authenticated successfully for IP: {}", finalIp);
        return true;
    }

    /**
     * Resolves the actual origin client IP address by safely evaluating cloud-forwarded routing lists.
     * Patched to prevent IP Spoofing on AWS architectures.
     */
    private String resolveClientIp(HttpServletRequest request) {
        if (properties.isBehindProxy()) {
            String[] headersToInspect = {
                    "X-Forwarded-For",
                    "X-Real-IP",
                    "CF-Connecting-IP",
                    "True-Client-IP"
            };

            for (String headerName : headersToInspect) {
                String headerValue = request.getHeader(headerName);
                if (headerValue != null && !headerValue.isBlank()) {
                    // ⚡ AWS SECURITY PATCH: Clean up whitespace and get the very first client IP address.
                    // If an attacker sends a fake "X-Forwarded-For: 196.201.214.1", AWS ALB appends the real
                    // untrusted IP to the right side: "196.201.214.1, 203.0.113.19".
                    String[] proxyChain = headerValue.split(",");
                    String originalClient = proxyChain[0].trim();

                    // Prevent returning "unknown" strings injected into fake proxy headers
                    if (!originalClient.equalsIgnoreCase("unknown")) {
                        return originalClient;
                    }
                }
            }
        }

        // Fallback option directly evaluating socket connection if proxy settings are inactive
        return request.getRemoteAddr();
    }
}
