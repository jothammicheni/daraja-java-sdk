package com.github.jothammicheni.daraja_springboot_starter_jdk.core.config;

import com.github.jothammicheni.daraja_springboot_starter_jdk.core.client.MpesaAuthInterceptor;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.service.DefaultMpesaClient;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.webhook.MpesaWebhookController;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.webhook.WebhookSecurityValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(MpesaProperties.class)
@EnableConfigurationProperties(MpesaProperties.class) // ⚡ FIXED: Cleanly maps your mpesa.daraja properties automatically
@Import({
        TokenCacheConfig.class,
        MpesaRestClientConfig.class,      // Loads your RestTemplate bean factory
        MpesaAuthInterceptor.class,       // ⚡ FIXED: Explicitly loads your core security interceptor component
        DefaultMpesaClient.class,         // ⚡ FIXED: Explicitly loads your main MpesaClient engine bean
        WebhookSecurityValidator.class,   // ⚡ FIXED: Explicitly loads your proxy firewall helper component
        MpesaWebhookController.class      // ⚡ FIXED: Explicitly loads your webhook async confirmation endpoints
})
public class MpesaAutoConfiguration {
    // Keep this class body completely clean. The annotations above map your entire library map.
}
