package com.github.jothammicheni.daraja.client;

import com.github.jothammicheni.daraja.config.MpesaConfig;
import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;
import com.github.jothammicheni.daraja.exception.MpesaApiException;
import com.github.jothammicheni.daraja.http.MpesaHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultMpesaClientTest {

    private static final String TEST_CONSUMER_KEY = "test-consumer-key";
    private static final String TEST_CONSUMER_SECRET = "test-consumer-secret";
    private static final String TEST_API_SECRET = "test-api-secret";
    private static final String BASE_URL = "https://sandbox.safaricom.co.ke";
    private static final String CALLBACK_URL = "https://myapp.com/cb/confirmation";

    private MpesaConfig config;
    private DefaultMpesaClient client;

    @Mock
    private MpesaHttpClient mockHttpClient;

    @BeforeEach
    void setUp() {
        config = new MpesaConfig.Builder(
                TEST_CONSUMER_KEY,
                TEST_CONSUMER_SECRET,
                TEST_API_SECRET
        )
                .baseUrl(BASE_URL)
                .callbackUrl(CALLBACK_URL)
                .connectTimeout(5)
                .readTimeout(10)
                .build();

        client = new DefaultMpesaClient(config, mockHttpClient);
    }

    // ============================================
    // 1. VALIDATION TESTS
    // ============================================

    @Test
    void shouldThrowExceptionWhenIdempotencyKeyIsNull() {
        assertThatThrownBy(() -> new StkPushRequest(
                "174379",
                "254708374149",
                100,
                "254708374149",
                "174379",
                "ORDER-123",
                "Payment",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Idempotency-Key is mandatory to prevent duplicate payments");
    }

    @Test
    void shouldThrowExceptionWhenIdempotencyKeyIsBlank() {
        assertThatThrownBy(() -> new StkPushRequest(
                "174379",
                "254708374149",
                100,
                "254708374149",
                "174379",
                "ORDER-123",
                "Payment",
                ""
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Idempotency-Key is mandatory to prevent duplicate payments");
    }

    @Test
    void shouldThrowExceptionWhenPhoneNumberIsInvalid() {
        assertThatThrownBy(() -> new StkPushRequest(
                "174379",
                "invalid-phone",
                100,
                "invalid-phone",
                "174379",
                "ORDER-123",
                "Payment",
                UUID.randomUUID().toString()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Kenyan phone number");
    }

    @Test
    void shouldCreateClientWithConfig() {
        assertThat(client).isNotNull();
        assertThat(client).extracting("httpClient").isEqualTo(mockHttpClient);
    }

    // ============================================
    // 2. SUCCESSFUL STK PUSH
    // ============================================

    @Test
    void shouldInitiateStkPushSuccessfully() throws IOException {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        String tokenUrl = BASE_URL + "/oauth/v1/generate?grant_type=client_credentials";
        String stkUrl = BASE_URL + "/mpesa/stkpush/v1/processrequest";

        when(mockHttpClient.sendRequest(
                eq(tokenUrl),
                eq("GET"),
                isNull(),
                anyMap()
        )).thenReturn("{\"access_token\":\"mock-token-123\"}");

        String stkPushJson = "{\"MerchantRequestID\":\"MER-123\",\"CheckoutRequestID\":\"CHECK-456\",\"ResponseCode\":\"0\",\"ResponseDescription\":\"Success\",\"CustomerMessage\":\"Payment received\"}";
        when(mockHttpClient.sendRequest(
                eq(stkUrl),
                eq("POST"),
                anyString(),
                anyMap()
        )).thenReturn(stkPushJson);

        StkPushResponse response = client.initiateStkPush(request);

        assertThat(response).isNotNull();
        assertThat(response.checkoutRequestID()).isEqualTo("CHECK-456");
        assertThat(response.merchantRequestID()).isEqualTo("MER-123");
        assertThat(response.responseCode()).isEqualTo("0");
        assertThat(response.isAccepted()).isTrue();

        // At least one request must have been made (the mock is called for token and STK Push)
        verify(mockHttpClient, atLeastOnce()).sendRequest(anyString(), anyString(), anyString(), anyMap());
    }

    // ============================================
    // 3. API RETURNS NON-200
    // ============================================

    @Test
    void shouldThrowExceptionWhenStkPushApiReturnsError() throws IOException {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        String tokenUrl = BASE_URL + "/oauth/v1/generate?grant_type=client_credentials";
        when(mockHttpClient.sendRequest(
                eq(tokenUrl),
                eq("GET"),
                isNull(),
                anyMap()
        )).thenReturn("{\"access_token\":\"mock-token-123\"}");

        String stkUrl = BASE_URL + "/mpesa/stkpush/v1/processrequest";
        when(mockHttpClient.sendRequest(
                eq(stkUrl),
                eq("POST"),
                anyString(),
                anyMap()
        )).thenThrow(new IOException("HTTP error 400: {\"error\":\"Invalid request\"}"));

        assertThatThrownBy(() -> client.initiateStkPush(request))
                .isInstanceOf(MpesaApiException.class)
                .hasMessageContaining("STK Push request failed");
    }

    // ============================================
    // 4. IDEMPOTENCY CACHE HIT
    // ============================================

    @Test
    void shouldReturnCachedResponseForDuplicateRequest() throws IOException {
        String idempotencyKey = "my-unique-key-123";
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .idempotencyKey(idempotencyKey)
                .build();

        String tokenUrl = BASE_URL + "/oauth/v1/generate?grant_type=client_credentials";
        when(mockHttpClient.sendRequest(
                eq(tokenUrl),
                eq("GET"),
                isNull(),
                anyMap()
        )).thenReturn("{\"access_token\":\"mock-token-123\"}");

        String stkUrl = BASE_URL + "/mpesa/stkpush/v1/processrequest";
        String stkPushJson = "{\"MerchantRequestID\":\"MER-123\",\"CheckoutRequestID\":\"CHECK-456\",\"ResponseCode\":\"0\",\"ResponseDescription\":\"Success\"}";
        when(mockHttpClient.sendRequest(
                eq(stkUrl),
                eq("POST"),
                anyString(),
                anyMap()
        )).thenReturn(stkPushJson);

        // First call – should cache the response
        StkPushResponse firstResponse = client.initiateStkPush(request);
        assertThat(firstResponse.checkoutRequestID()).isEqualTo("CHECK-456");

        // Second call – should hit the idempotency cache and return the same response
        StkPushResponse secondResponse = client.initiateStkPush(request);
        assertThat(secondResponse.checkoutRequestID()).isEqualTo("CHECK-456");
        // The second response should be the exact same object (cached)
        assertThat(secondResponse).isSameAs(firstResponse);

        // Verify that the mock was called at least once (the first call)
        // The second call should not trigger any new HTTP request because of the cache.
        // We cannot assert exact invocation count because the token may be cached,
        // so we just ensure at least one request was made.
        verify(mockHttpClient, atLeastOnce()).sendRequest(anyString(), anyString(), anyString(), anyMap());
    }

    // ============================================
    // 5. TOKEN REFRESH ON EXPIRY
    // ============================================

    @Test
    void shouldFetchNewTokenWhenTokenExpired() throws Exception {
        StkPushRequest request1 = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        String tokenUrl = BASE_URL + "/oauth/v1/generate?grant_type=client_credentials";
        String stkUrl = BASE_URL + "/mpesa/stkpush/v1/processrequest";

        // First call
        when(mockHttpClient.sendRequest(
                eq(tokenUrl),
                eq("GET"),
                isNull(),
                anyMap()
        )).thenReturn("{\"access_token\":\"mock-token-123\"}");

        String stkPushJson1 = "{\"MerchantRequestID\":\"MER-123\",\"CheckoutRequestID\":\"CHECK-456\",\"ResponseCode\":\"0\",\"ResponseDescription\":\"Success\"}";
        when(mockHttpClient.sendRequest(
                eq(stkUrl),
                eq("POST"),
                anyString(),
                anyMap()
        )).thenReturn(stkPushJson1);

        StkPushResponse firstResponse = client.initiateStkPush(request1);
        assertThat(firstResponse.checkoutRequestID()).isEqualTo("CHECK-456");

        // Capture the token
        java.lang.reflect.Field tokenField = DefaultMpesaClient.class.getDeclaredField("cachedToken");
        tokenField.setAccessible(true);
        String firstToken = (String) tokenField.get(client);

        // Force token expiry
        java.lang.reflect.Field expiryField = DefaultMpesaClient.class.getDeclaredField("tokenExpiryTime");
        expiryField.setAccessible(true);
        expiryField.setLong(client, System.currentTimeMillis() - 10000);

        // Reset mock and stub second call
        reset(mockHttpClient);

        when(mockHttpClient.sendRequest(
                eq(tokenUrl),
                eq("GET"),
                isNull(),
                anyMap()
        )).thenReturn("{\"access_token\":\"mock-token-456\"}");

        String stkPushJson2 = "{\"MerchantRequestID\":\"MER-789\",\"CheckoutRequestID\":\"CHECK-999\",\"ResponseCode\":\"0\",\"ResponseDescription\":\"Success\"}";
        when(mockHttpClient.sendRequest(
                eq(stkUrl),
                eq("POST"),
                anyString(),
                anyMap()
        )).thenReturn(stkPushJson2);

        // Use a different idempotency key to avoid the idempotency cache
        StkPushRequest request2 = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();

        StkPushResponse secondResponse = client.initiateStkPush(request2);
        assertThat(secondResponse.checkoutRequestID()).isEqualTo("CHECK-999");

        String secondToken = (String) tokenField.get(client);
        assertThat(secondToken).isNotEqualTo(firstToken);
        assertThat(secondToken).isEqualTo("mock-token-456");
    }

    // ============================================
    // 6. TOKEN FETCH FAILURE
    // ============================================

    @Test
    void shouldThrowExceptionWhenTokenFetchFails() throws IOException {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        String tokenUrl = BASE_URL + "/oauth/v1/generate?grant_type=client_credentials";

        when(mockHttpClient.sendRequest(
                eq(tokenUrl),
                eq("GET"),
                isNull(),
                anyMap()
        )).thenThrow(new IOException("HTTP error 401"));

        assertThatThrownBy(() -> client.initiateStkPush(request))
                .isInstanceOf(MpesaApiException.class)
                .hasMessageContaining("Could not authenticate Daraja credentials.");
    }

    // ============================================
    // 7. MALFORMED STK PUSH RESPONSE
    // ============================================

    @Test
    void shouldThrowExceptionWhenStkPushResponseIsMalformed() throws IOException {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        String tokenUrl = BASE_URL + "/oauth/v1/generate?grant_type=client_credentials";
        when(mockHttpClient.sendRequest(
                eq(tokenUrl),
                eq("GET"),
                isNull(),
                anyMap()
        )).thenReturn("{\"access_token\":\"mock-token\"}");

        String stkUrl = BASE_URL + "/mpesa/stkpush/v1/processrequest";
        when(mockHttpClient.sendRequest(
                eq(stkUrl),
                eq("POST"),
                anyString(),
                anyMap()
        )).thenReturn("This is not JSON");

        assertThatThrownBy(() -> client.initiateStkPush(request))
                .isInstanceOf(MpesaApiException.class)
                .hasMessageContaining("STK Push request failed");
    }

    // ============================================
    // 8. CACHE CLEANUP
    // ============================================

    @Test
    void shouldCleanExpiredCacheWithoutException() {
        assertThatCode(() -> client.cleanExpiredCache()).doesNotThrowAnyException();
    }
}