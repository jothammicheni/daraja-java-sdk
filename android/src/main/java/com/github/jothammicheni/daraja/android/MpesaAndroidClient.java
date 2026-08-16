package com.github.jothammicheni.daraja.android;

import com.github.jothammicheni.daraja.client.DefaultMpesaClient;
import com.github.jothammicheni.daraja.client.MpesaClient;
import com.github.jothammicheni.daraja.config.MpesaConfig;
import com.github.jothammicheni.daraja.http.MpesaHttpClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Android-optimized M-Pesa client.
 * Auto-configures OkHttp for Android developers - zero boilerplate.
 *
 * <p>Usage:
 * <pre>
 * MpesaClient client = MpesaAndroidClient.create(config);
 * </pre>
 */
public final class MpesaAndroidClient {

    private MpesaAndroidClient() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a new M-Pesa client with default OkHttp settings.
     *
     * @param config M-Pesa configuration
     * @return a configured M-Pesa client
     */
    public static MpesaClient create(MpesaConfig config) {
        return new DefaultMpesaClient(config, new AndroidHttpClient(
                config.getConnectTimeout(),
                config.getReadTimeout()
        ));
    }

    /**
     * Creates a new M-Pesa client with a custom OkHttp client.
     *
     * @param config       M-Pesa configuration
     * @param okHttpClient your own OkHttp instance (for advanced customization)
     * @return a configured M-Pesa client
     */
    public static MpesaClient create(MpesaConfig config, OkHttpClient okHttpClient) {
        return new DefaultMpesaClient(config, new AndroidHttpClient(okHttpClient));
    }

    /**
     * Internal Android HTTP client that adapts OkHttp to the SDK's interface.
     */
    private static class AndroidHttpClient implements MpesaHttpClient {
        private final OkHttpClient client;

        AndroidHttpClient(int connectTimeout, int readTimeout) {
            this.client = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .build();
        }

        AndroidHttpClient(OkHttpClient client) {
            this.client = client;
        }

        @Override
        public String sendRequest(String url, String method, String body, Map<String, String> headers) throws IOException {
            Request.Builder builder = new Request.Builder().url(url);
            headers.forEach(builder::addHeader);

            if (body != null && !body.isEmpty()) {
                builder.method(method, RequestBody.create(
                        body,
                        MediaType.parse("application/json")
                ));
            } else {
                builder.method(method, null);
            }

            try (Response response = client.newCall(builder.build()).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP error: " + response.code() +
                            (response.body() != null ? " - " + response.body().string() : ""));
                }
                return response.body() != null ? response.body().string() : "";
            }
        }
    }
}