package com.github.jothammicheni.daraja.client;

import com.github.jothammicheni.daraja.config.MpesaConfig;
import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;
import com.github.jothammicheni.daraja.exception.MpesaApiException;
import com.github.jothammicheni.daraja.http.JavaHttpClient;
import com.github.jothammicheni.daraja.http.MpesaHttpClient;
import com.github.jothammicheni.daraja.util.PhoneNumberUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of the M-Pesa client.
 * Supports both Java SE (using built-in HttpClient) and Android (via pluggable HTTP client).
 */
public class DefaultMpesaClient implements MpesaClient {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(DefaultMpesaClient.class.getName());

    // JSON extraction patterns (Jackson removed)
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern MERCHANT_REQUEST_ID_PATTERN = Pattern.compile("\"MerchantRequestID\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern CHECKOUT_REQUEST_ID_PATTERN = Pattern.compile("\"CheckoutRequestID\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern RESPONSE_CODE_PATTERN = Pattern.compile("\"ResponseCode\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern RESPONSE_DESCRIPTION_PATTERN = Pattern.compile("\"ResponseDescription\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern CUSTOMER_MESSAGE_PATTERN = Pattern.compile("\"CustomerMessage\"\\s*:\\s*\"([^\"]*)\"");

    private final MpesaConfig config;
    private final MpesaHttpClient httpClient;          // Pluggable HTTP client (Java SE, Android, etc.)
    private final Map<String, CachedResponse> idempotencyCache;
    private String cachedToken;
    private long tokenExpiryTime;

    private static class CachedResponse {
        private final StkPushResponse response;
        private final long timestamp;

        public CachedResponse(StkPushResponse response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired(long ttlMillis) {
            return System.currentTimeMillis() - timestamp > ttlMillis;
        }

        public StkPushResponse getResponse() {
            return response;
        }
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    /**
     * Creates a new client with the default Java SE HTTP client.
     * Works on JDK 11+ environments (Spring Boot, Quarkus, standalone Java).
     *
     * @param config M-Pesa configuration
     */
    public DefaultMpesaClient(MpesaConfig config) {
        this(config, new JavaHttpClient(config.getConnectTimeout(), config.getReadTimeout()));
    }

    /**
     * Creates a new client with a custom HTTP client.
     * Use this on Android (with OkHttp) or for testing (with mocks).
     *
     * @param config     M-Pesa configuration
     * @param httpClient your own implementation of {@link MpesaHttpClient}
     */
    public DefaultMpesaClient(MpesaConfig config, MpesaHttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.idempotencyCache = new ConcurrentHashMap<>();
    }

    // Package-private constructor for testing (allows injecting mock HttpClient)
    // Kept for backward compatibility with existing tests, but now wraps it via a simple adapter.
    DefaultMpesaClient(MpesaConfig config, java.net.http.HttpClient httpClient) {
        this(config, new JavaHttpClient(httpClient));
    }

    // ============================================
    // CORE PAYMENT METHOD
    // ============================================

    @Override
    public StkPushResponse initiateStkPush(StkPushRequest request) {
        // 1. PHONE NUMBER VALIDATION & NORMALIZATION
        String rawPhoneNumber = request.phoneNumber();

        if (!PhoneNumberUtils.isValidKenyanPhone(rawPhoneNumber)) {
            String maskedPhone = PhoneNumberUtils.maskPhoneNumber(rawPhoneNumber);
            log.severe("❌ Invalid Kenyan phone number: " + maskedPhone);
            throw new MpesaApiException(
                    "Invalid Kenyan phone number: " + maskedPhone +
                            ". Must be in format: +2547XXXXXXXX, 2547XXXXXXXX, 07XXXXXXXX, or 7XXXXXXXX"
            );
        }

        String normalizedPhone = PhoneNumberUtils.normalizeKenyanPhone(rawPhoneNumber);
        String maskedPhone = PhoneNumberUtils.maskPhoneNumber(rawPhoneNumber);
        PhoneNumberUtils.PhoneType phoneType = PhoneNumberUtils.detectPhoneType(rawPhoneNumber);

        log.info("📱 Processing STK Push for phone: " + maskedPhone);
        log.info("📱 Phone type: " + phoneType.getDisplayName());

        // 2. IDEMPOTENCY CHECK
        String idempotencyKey = request.idempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MpesaApiException("Idempotency-Key is mandatory to prevent duplicate payments");
        }

        CachedResponse cached = idempotencyCache.get(idempotencyKey);
        if (cached != null) {
            if (!cached.isExpired(Duration.ofHours(24).toMillis())) {
                log.warning("⚠️ Duplicate request detected for key: " + idempotencyKey + " - Phone: " + maskedPhone + ". Returning cached response.");
                return cached.getResponse();
            } else {
                idempotencyCache.remove(idempotencyKey);
                log.info("🗑️ Expired idempotency entry removed for key: " + idempotencyKey);
            }
        }

        // 3. GET ACCESS TOKEN
        String accessToken = getAccessToken();

        // 4. GENERATE STK PASSWORD
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String password = request.businessShortCode() + config.getApiSecret() + timestamp;
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());

        // 5. PREPARE PAYLOAD
        Map<String, Object> payload = new HashMap<>();
        payload.put("BusinessShortCode", request.businessShortCode());
        payload.put("Password", encodedPassword);
        payload.put("Timestamp", timestamp);
        payload.put("TransactionType", "CustomerPayBillOnline");
        payload.put("Amount", request.amount());
        payload.put("PartyA", normalizedPhone);
        payload.put("PartyB", request.businessShortCode());
        payload.put("PhoneNumber", normalizedPhone);
        payload.put("CallBackURL", config.getCallbackUrl());
        payload.put("AccountReference", request.accountReference());
        payload.put("TransactionDesc", request.transactionDesc());

        try {
            String jsonPayload = payloadToJson(payload);
            log.info("📤 Sending STK Push request for phone: " + maskedPhone + " | Amount: " + request.amount());

            // Prepare headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + accessToken);

            // Execute request via the pluggable HTTP client
            String responseBody = httpClient.sendRequest(
                    config.getBaseUrl() + "/mpesa/stkpush/v1/processrequest",
                    "POST",
                    jsonPayload,
                    headers
            );

            // Parse JSON manually (NO Jackson!)
            StkPushResponse stkResponse = parseStkPushResponse(responseBody);

            // DECORATE AND CACHE ONLY SUCCESSFUL RESPONSES
            if (stkResponse != null) {
                StkPushResponse decoratedResponse = stkResponse.withIdempotencyKey(idempotencyKey);

                if (stkResponse.isAccepted()) {
                    idempotencyCache.put(idempotencyKey, new CachedResponse(decoratedResponse));
                    log.info("✅ STK Push initiated successfully for phone: " + maskedPhone + " | CheckoutID: " + stkResponse.checkoutRequestID());
                } else {
                    log.warning("⚠️ STK Push rejected for phone: " + maskedPhone + " | Reason: " + stkResponse.responseDescription());
                }

                return decoratedResponse;
            }

            return stkResponse;

        } catch (Exception e) {
            log.severe("❌ STK Push failed for phone: " + maskedPhone + " | Account: " + request.accountReference() + " - " + e.getMessage());
            throw new MpesaApiException("STK Push request failed. Please retry with the same Idempotency-Key.", e);
        }
    }

    // ============================================
    // TOKEN MANAGEMENT
    // ============================================

    private synchronized String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedToken;
        }
        return fetchNewToken();
    }

    private String fetchNewToken() {
        log.info("🔑 Token expired or missing. Fetching a fresh OAuth token from Safaricom Daraja...");

        try {
            String credentials = config.getConsumerKey() + ":" + config.getConsumerSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + encodedCredentials);

            String url = config.getBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials";
            String responseBody = httpClient.sendRequest(url, "GET", null, headers);

            String accessToken = extractAccessToken(responseBody);
            if (accessToken != null) {
                cachedToken = accessToken;
                tokenExpiryTime = System.currentTimeMillis() + (55 * 60 * 1000); // 55 minutes
                log.info("✅ Token acquired successfully");
                return cachedToken;
            }

            throw new MpesaApiException("Safaricom response did not contain an access_token.");

        } catch (Exception e) {
            log.severe("❌ Failed to authenticate with Safaricom Daraja OAuth endpoint: " + e.getMessage());
            throw new MpesaApiException("Could not authenticate Daraja credentials.", e);
        }
    }

    // ============================================
    // MANUAL JSON PARSING (NO JACKSON!)
    // ============================================

    private String extractAccessToken(String json) {
        Matcher m = TOKEN_PATTERN.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private StkPushResponse parseStkPushResponse(String json) {
        String merchantId = extractFirst(MERCHANT_REQUEST_ID_PATTERN, json);
        String checkoutId = extractFirst(CHECKOUT_REQUEST_ID_PATTERN, json);
        String responseCode = extractFirst(RESPONSE_CODE_PATTERN, json);
        String responseDesc = extractFirst(RESPONSE_DESCRIPTION_PATTERN, json);
        String customerMsg = extractFirst(CUSTOMER_MESSAGE_PATTERN, json);

        if (merchantId == null && checkoutId == null && responseCode == null) {
            log.warning("⚠️ Could not parse STK Push response: " + json);
            throw new MpesaApiException("Failed to parse M-Pesa response: " + json);
        }

        return new StkPushResponse(
                merchantId != null ? merchantId : "",
                checkoutId != null ? checkoutId : "",
                responseCode != null ? responseCode : "",
                responseDesc != null ? responseDesc : "",
                customerMsg != null ? customerMsg : "",
                null,  // idempotencyKey (set later)
                null   // timestamp (set later)
        );
    }

    private String extractFirst(Pattern pattern, String input) {
        Matcher m = pattern.matcher(input);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String payloadToJson(Map<String, Object> payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int count = 0;
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (count > 0) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("\"").append(value).append("\"");
            }
            count++;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void cleanExpiredCache() {
        long ttl = Duration.ofHours(24).toMillis();
        idempotencyCache.entrySet().removeIf(entry -> entry.getValue().isExpired(ttl));
    }
}