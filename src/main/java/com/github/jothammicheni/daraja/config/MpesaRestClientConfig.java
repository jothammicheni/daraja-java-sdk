package com.github.jothammicheni.daraja_springboot_starter_jdk.core.config;

import com.github.jothammicheni.daraja_springboot_starter_jdk.core.client.MpesaAuthInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class MpesaRestClientConfig {

    @Bean
    public RestTemplate mpesaRestTemplate(
            MpesaProperties properties,          // ✅ no qualifier needed
            MpesaAuthInterceptor authInterceptor // ✅ injected directly
    ) {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(properties.connectTimeout()))
                .setReadTimeout(Duration.ofSeconds(properties.readTimeout()))
                .additionalInterceptors(Collections.singletonList(authInterceptor))
                .build();
    }
}