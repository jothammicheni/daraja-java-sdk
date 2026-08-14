package com.github.jothammicheni.daraja.client;

import com.github.jothammicheni.daraja.config.MpesaConfig;
import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;
import com.github.jothammicheni.daraja.exception.MpesaApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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
    private HttpClient mockHttpClient;

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
        assertThat(client).hasFieldOrPropertyWithValue("httpClient", mockHttpClient);
    }

    // ============================================
    // 2. SUCCESSFUL STK PUSH
    // ============================================

    @Test
    void shouldInitiateStkPushSuccessfully() throws IOException, InterruptedException {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        String tokenJson = "{\"access_token\":\"mock-token-123\"}";
        HttpResponse<String> tokenResponse = mockHttpResponse(200, tokenJson);

        String stkPushJson = "{\"MerchantRequestID\":\"MER-123\",\"CheckoutRequestID\":\"CHECK-456\",\"ResponseCode\":\"0\",\"ResponseDescription\":\"Success\",\"CustomerMessage\":\"Payment received\"}";
        HttpResponse<String> stkPushResponse = mockHttpResponse(200, stkPushJson);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResponse, stkPushResponse);

        StkPushResponse response = client.initiateStkPush(request);

        assertThat(response).isNotNull();
        assertThat(response.checkoutRequestID()).isEqualTo("CHECK-456");
        assertThat(response.merchantRequestID()).isEqualTo("MER-123");
        assertThat(response.responseCode()).isEqualTo("0");
        assertThat(response.isAccepted()).isTrue();
    }

    // ============================================
    // 3. API RETURNS NON-200
    // ============================================

    @Test
    void shouldThrowExceptionWhenStkPushApiReturnsError() throws IOException, InterruptedException {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        String tokenJson = "{\"access_token\":\"mock-token-123\"}";
        HttpResponse<String> tokenResponse = mockHttpResponse(200, tokenJson);

        HttpResponse<String> stkPushResponse = mockHttpResponse(400, "{\"error\":\"Invalid request\"}");

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResponse, stkPushResponse);

        assertThatThrownBy(() -> client.initiateStkPush(request))
                .isInstanceOf(MpesaApiException.class)
                .hasMessageContaining("STK Push request failed");
    }

    // ============================================
    // 4. IDEMPOTENCY CACHE HIT
    // ============================================

    @Test
    void shouldReturnCachedResponseForDuplicateRequest() throws IOException, InterruptedException {
        String idempotencyKey = "my-unique-key-123";
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .idempotencyKey(idempotencyKey)
                .build();

        String tokenJson = "{\"access_token\":\"mock-token-123\"}";
        HttpResponse<String> tokenResponse = mockHttpResponse(200, tokenJson);

        String stkPushJson = "{\"MerchantRequestID\":\"MER-123\",\"CheckoutRequestID\":\"CHECK-456\",\"ResponseCode\":\"0\",\"ResponseDescription\":\"Success\"}";
        HttpResponse<String> stkPushResponse = mockHttpResponse(200, stkPushJson);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResponse, stkPushResponse);

        StkPushResponse firstResponse = client.initiateStkPush(request);
        assertThat(firstResponse.checkoutRequestID()).isEqualTo("CHECK-456");

        StkPushResponse secondResponse = client.initiateStkPush(request);
        assertThat(secondResponse.checkoutRequestID()).isEqualTo("CHECK-456");
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

        // First call – token + STK Push
        String tokenJson1 = "{\"access_token\":\"mock-token-123\"}";
        HttpResponse<String> tokenResponse1 = mockHttpResponse(200, tokenJson1);
        String stkPushJson1 = "{\"MerchantRequestID\":\"MER-123\",\"CheckoutRequestID\":\"CHECK-456\",\"ResponseCode\":\"0\",\"ResponseDescription\":\"Success\"}";
        HttpResponse<String> stkPushResponse1 = mockHttpResponse(200, stkPushJson1);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResponse1, stkPushResponse1);

        StkPushResponse firstResponse = client.initiateStkPush(request1);
        assertThat(firstResponse.checkoutRequestID()).isEqualTo("CHECK-456");

        // Capture the cached token after first call
        java.lang.reflect.Field tokenField = DefaultMpesaClient.class.getDeclaredField("cachedToken");
        tokenField.setAccessible(true);
        String firstToken = (String) tokenField.get(client);

        // Force token expiry
        java.lang.reflect.Field expiryField = DefaultMpesaClient.class.getDeclaredField("tokenExpiryTime");
        expiryField.setAccessible(true);
        expiryField.setLong(client, System.currentTimeMillis() - 10000);

        // Reset mock and stub the second call sequence
        reset(mockHttpClient);
        String tokenJson2 = "{\"access_token\":\"mock-token-456\"}";
        HttpResponse<String> tokenResponse2 = mockHttpResponse(200, tokenJson2);
        String stkPushJson2 = "{\"MerchantRequestID\":\"MER-789\",\"CheckoutRequestID\":\"CHECK-999\",\"ResponseCode\":\"0\",\"ResponseDescription\":\"Success\"}";
        HttpResponse<String> stkPushResponse2 = mockHttpResponse(200, stkPushJson2);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResponse2, stkPushResponse2);

        // IMPORTANT: use a distinct idempotency key for the second logical
        // request. Reusing request1's key here would hit the SDK's
        // idempotency cache and return the cached first response WITHOUT
        // calling httpClient.send() again - which is correct SDK behavior,
        // but defeats the point of this test (verifying token refresh
        // actually triggers a fresh HTTP round-trip).
        StkPushRequest request2 = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();

        StkPushResponse secondResponse = client.initiateStkPush(request2);
        assertThat(secondResponse.checkoutRequestID()).isEqualTo("CHECK-999");

        // Verify that the cached token has changed
        String secondToken = (String) tokenField.get(client);
        assertThat(secondToken).isNotEqualTo(firstToken);
        assertThat(secondToken).isEqualTo("mock-token-456");
    }

    // ============================================
    // 6. TOKEN FETCH FAILURE
    // ============================================

    @Test
    void shouldThrowExceptionWhenTokenFetchFails() throws IOException, InterruptedException {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        // 401 response - fetchNewToken() only reads statusCode() in this
        // branch and never calls body(), so mockHttpResponse()'s lenient
        // stubbing (see helper below) is what keeps this test passing
        // under Mockito's strict-stubs mode.
        HttpResponse<String> tokenResponse = mockHttpResponse(401, "Unauthorized");

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResponse);

        assertThatThrownBy(() -> client.initiateStkPush(request))
                .isInstanceOf(MpesaApiException.class)
                .hasMessageContaining("Could not authenticate Daraja credentials.");
    }

    // ============================================
    // 7. MALFORMED STK PUSH RESPONSE
    // ============================================

    @Test
    void shouldThrowExceptionWhenStkPushResponseIsMalformed() throws IOException, InterruptedException {
        StkPushRequest request = StkPushRequest.builder()
                .businessShortCode("174379")
                .phoneNumber("254708374149")
                .amount(100)
                .accountReference("ORDER-123")
                .build();

        String tokenJson = "{\"access_token\":\"mock-token\"}";
        HttpResponse<String> tokenResponse = mockHttpResponse(200, tokenJson);

        String malformedJson = "This is not JSON";
        HttpResponse<String> stkPushResponse = mockHttpResponse(200, malformedJson);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResponse, stkPushResponse);

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

    // Helper method to create mocked HttpResponse.
    // Both stubs are lenient because not every test path reads both
    // statusCode() and body() - e.g. fetchNewToken() only calls body()
    // after a 200 status check, so a 401-mock's body() stub would
    // otherwise be flagged as an unnecessary stubbing under Mockito's
    // strict-stubs mode (the MockitoExtension default).
    private static <T> HttpResponse<T> mockHttpResponse(int statusCode, T body) {
        @SuppressWarnings("unchecked")
        HttpResponse<T> response = mock(HttpResponse.class);
        lenient().when(response.statusCode()).thenReturn(statusCode);
        lenient().when(response.body()).thenReturn(body);
        return response;
    }
}