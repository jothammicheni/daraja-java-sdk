package com.github.jothammicheni.daraja.http;

import java.io.IOException;
import java.util.Map;

/**
 * Pluggable HTTP client interface for the Daraja M-Pesa SDK.
 *
 * <p>This interface allows the SDK to work with any HTTP client library
 * (Java SE built-in, OkHttp, Apache HttpClient, etc.) without forcing
 * a specific dependency on the user.</p>
 *
 * <p>Implementations must handle the full HTTP request lifecycle including
 * connection timeouts, read timeouts, and error handling.</p>
 *
 * @author Jotham Micheni
 * @version 1.0.0
 */
public interface MpesaHttpClient {

    /**
     * Sends an HTTP request and returns the response body as a string.
     *
     * @param url     the request URL (including query parameters if needed)
     * @param method  the HTTP method (GET, POST, PUT, DELETE, etc.)
     * @param body    the request body (may be null or empty for GET/DELETE)
     * @param headers a map of HTTP headers (may be empty)
     * @return the response body as a string
     * @throws IOException if the request fails or the response status code is not 2xx
     */
    String sendRequest(String url, String method, String body, Map<String, String> headers) throws IOException;
}