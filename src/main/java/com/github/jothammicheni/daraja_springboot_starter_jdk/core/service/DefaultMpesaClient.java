package com.github.jothammicheni.daraja_springboot_starter_jdk.core.service;

import com.github.jothammicheni.daraja_springboot_starter_jdk.core.config.MpesaProperties;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto.StkPushRequest;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto.StkPushResponse;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.exception.MpesaApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Component
public class DefaultMpesaClient implements MpesaClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultMpesaClient.class);
    private final RestTemplate restTemplate;
    private final MpesaProperties properties;
    private final CacheManager cacheManager;

    /**
     * Complete production constructor.
     * Uses lowercase "mpesaRestTemplate" qualifier to match the actual configuration bean exactly.
     * Uses "mpesaProperties" qualifier to bypass any parameter name collection stripping limitations.
     */
    public DefaultMpesaClient(
            @Qualifier("mpesaRestTemplate") RestTemplate mpesaRestTemplate,
            @Qualifier("mpesaProperties") MpesaProperties properties,
            CacheManager cacheManager) {
        this.restTemplate = mpesaRestTemplate;
        this.properties = properties;
        this.cacheManager = cacheManager;
    }

    @Override
    public StkPushResponse initiateStkPush(StkPushRequest request) {
        // 1. IDEMPOTENCY CHECK (Reads from 'mpesa-idempotency' bucket)
        String idempotencyKey = request.idempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MpesaApiException("Idempotency-Key is mandatory to prevent duplicate payments");
        }

        Cache idempotencyCache = cacheManager.getCache("mpesa-idempotency");
        if (idempotencyCache != null) {
            Cache.ValueWrapper cachedResponse = idempotencyCache.get(idempotencyKey);
            if (cachedResponse != null) {
                log.warn("⚠️ Duplicate request detected for key: {}. Returning cached response.", idempotencyKey);
                return (StkPushResponse) cachedResponse.get();
            }
        }

        // 2. FETCH ACCESS TOKEN (Reads from or populates 'mpesa-token' bucket)
        String accessToken = fetchCachedOrFreshToken();

        // 3. GENERATE STK PASSWORD
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String passwordSource = request.businessShortCode() + properties.apiSecret() + timestamp;
        String encodedPassword = Base64.getEncoder().encodeToString(passwordSource.getBytes(StandardCharsets.UTF_8));

        // 4. PREPARE SECURE HEADERS
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // 5. PAYLOAD STRUCTURE
        Map<String, Object> payload = Map.ofEntries(
                Map.entry("BusinessShortCode", request.businessShortCode()),
                Map.entry("Password", encodedPassword),
                Map.entry("Timestamp", timestamp),
                Map.entry("TransactionType", "CustomerPayBillOnline"),
                Map.entry("Amount", request.amount()),
                Map.entry("PartyA", request.phoneNumber()),
                Map.entry("PartyB", request.businessShortCode()),
                Map.entry("PhoneNumber", request.phoneNumber()),
                Map.entry("CallBackURL", properties.callbackUrl()),
                Map.entry("AccountReference", request.accountReference()),
                Map.entry("TransactionDesc", request.transactionDesc())
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        String url = properties.baseUrl() + "/mpesa/stkpush/v1/processrequest";

        try {
            ResponseEntity<StkPushResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, StkPushResponse.class);

            StkPushResponse responseBody = response.getBody();

            // 6. DECORATE AND CACHE STK TRANSACTION
            if (responseBody != null) {
                StkPushResponse decoratedResponse = responseBody.withIdempotencyKey(idempotencyKey);

                if (idempotencyCache != null) {
                    idempotencyCache.put(idempotencyKey, decoratedResponse);
                    log.info("✅ STK Push initiated and cached. CheckoutID: {}", responseBody.checkoutRequestID());
                }

                return decoratedResponse;
            }

            return responseBody;

        } catch (Exception e) {
            log.error("STK Push failed for Account: {}", request.accountReference(), e);
            throw new MpesaApiException("STK Push request failed. Please retry with the same Idempotency-Key.", e);
        }
    }

    /**
     * Checks your TokenCacheConfig 'mpesa-token' bucket.
     * Keeps you safe from extra files and avoids local proxy interception bugs.
     */
    private String fetchCachedOrFreshToken() {
        Cache tokenCache = cacheManager.getCache("mpesa-token");
        if (tokenCache != null) {
            Cache.ValueWrapper cachedToken = tokenCache.get("token-key");
            if (cachedToken != null) {
                return (String) cachedToken.get();
            }
        }

        // Cache miss -> Fetch directly from Safaricom API
        String freshToken = generateFreshMpesaToken();
        if (tokenCache != null && freshToken != null) {
            tokenCache.put("token-key", freshToken);
        }
        return freshToken;
    }

    /**
     * Communicates with Daraja OAuth gateway to generate a fresh token string
     */
    @SuppressWarnings("unchecked")
    private String generateFreshMpesaToken() {
        log.info("🔑 Token expired or missing. Fetching a fresh OAuth token from Safaricom Daraja...");

        String credentials = properties.consumerKey() + ":" + properties.consumerSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = properties.baseUrl() + "/oauth/v1/generate?grant_type=client_credentials";

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("access_token")) {
                return (String) body.get("access_token");
            }
            throw new MpesaApiException("Safaricom response did not contain an access_token.");
        } catch (Exception e) {
            log.error("❌ Failed to authenticate with Safaricom Daraja OAuth endpoint", e);
            throw new MpesaApiException("Could not authenticate Daraja credentials.", e);
        }
    }
}
