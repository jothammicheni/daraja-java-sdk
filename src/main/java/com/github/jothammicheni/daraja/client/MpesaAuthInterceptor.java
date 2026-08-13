package com.github.jothammicheni.daraja_springboot_starter_jdk.core.client;

import com.github.jothammicheni.daraja_springboot_starter_jdk.core.config.MpesaProperties;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class MpesaAuthInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MpesaAuthInterceptor.class);
    private final RestTemplate restTemplate;

    // ⚡ FIXED: Added @Qualifier to explicitly pick your auto-configuration method bean name
    @Autowired
    @Qualifier("mpesaProperties")
    private MpesaProperties properties;

    @Autowired
    private CacheManager cacheManager;

    public MpesaAuthInterceptor() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        String token = getAccessTokenFromCache();
        request.getHeaders().setBearerAuth(token);

        ClientHttpResponse response = execution.execute(request, body);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            log.warn("M-Pesa token invalid or expired prematurely. Evicting cache and retrying...");
            evictTokenCache();
            token = getAccessTokenFromCache();
            request.getHeaders().setBearerAuth(token);
            return execution.execute(request, body);
        }
        return response;
    }

    private String getAccessTokenFromCache() {
        Cache cache = cacheManager.getCache("mpesa-token");
        if (cache != null) {
            return cache.get("token-key", this::fetchAccessTokenDirectly);
        }
        return fetchAccessTokenDirectly();
    }

    private void evictTokenCache() {
        Cache cache = cacheManager.getCache("mpesa-token");
        if (cache != null) {
            cache.evict("token-key");
        }
    }

    private String fetchAccessTokenDirectly() {
        log.info("Fetching new OAuth token from Safaricom Daraja API (Cache Miss)");
        String credentials = properties.consumerKey() + ":" + properties.consumerSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = properties.baseUrl() + "/oauth/v1/generate?grant_type=client_credentials";

        try {
            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, AccessTokenResponse.class);
            if (response.getBody() != null && response.getBody().accessToken() != null) {
                log.info("M-Pesa Token successfully acquired.");
                return response.getBody().accessToken();
            }
            throw new RuntimeException("Empty token response received from Safaricom gateway");
        } catch (Exception e) {
            log.error("Failed to acquire OAuth token from Safaricom", e);
            throw new RuntimeException("Could not authenticate with M-Pesa", e);
        }
    }
}
