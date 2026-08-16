package com.github.jothammicheni.daraja.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Implementation of {@link MpesaHttpClient} using the built-in
 * {@link java.net.http.HttpClient} from Java 11+.
 *
 * <p>This client is suitable for Java SE environments (Spring Boot,
 * Quarkus, standalone applications) but is NOT compatible with Android
 * (which does not include java.net.http.HttpClient).</p>
 *
 * @author Jotham Micheni
 * @version 1.0.0
 * @see MpesaHttpClient
 */
public class JavaHttpClient implements MpesaHttpClient {

    private final HttpClient client;

    /**
     * Constructs a new Java HTTP client with custom timeouts.
     *
     * @param connectTimeout connection timeout in seconds
     * @param readTimeout    read timeout in seconds
     */
    public JavaHttpClient(int connectTimeout, int readTimeout) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();
    }

    /**
     * Constructs a new Java HTTP client from an existing HttpClient instance.
     * Used primarily for testing (allowing mock injection) and for the
     * package-private test constructor in the main SDK.
     *
     * @param httpClient an existing {@link HttpClient} instance
     */
    public JavaHttpClient(HttpClient httpClient) {
        this.client = httpClient;
    }

    @Override
    public String sendRequest(String url, String method, String body, Map<String, String> headers) throws IOException {
        // Validate inputs
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("HTTP method cannot be null or empty");
        }

        // Build the request
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));

        // Add headers
        if (headers != null) {
            headers.forEach(builder::header);
        }

        // Set body or no body
        if (body != null && !body.isEmpty()) {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpRequest request = builder.build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check for successful status code (2xx)
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }

            // Throw an exception with details on non-2xx responses
            throw new IOException(String.format(
                    "HTTP error %d: %s (URL: %s)",
                    response.statusCode(),
                    response.body() != null ? response.body() : "No response body",
                    url
            ));

        } catch (InterruptedException e) {
            // Restore interrupted status and wrap as IOException
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request was interrupted", e);
        }
    }
}