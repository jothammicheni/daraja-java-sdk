package com.github.jothammicheni.daraja.dto.webhook;

import java.util.Map;

/**
 * Pure Java DTO - No Spring annotations!
 */
public class WebhookResponse {

    private final String resultCode;
    private final String resultDesc;

    private WebhookResponse(String resultCode, String resultDesc) {
        this.resultCode = resultCode;
        this.resultDesc = resultDesc;
    }

    public String getResultCode() { return resultCode; }
    public String getResultDesc() { return resultDesc; }

    public static WebhookResponse success() {
        return new WebhookResponse("0", "Success");
    }

    public static WebhookResponse success(String message) {
        return new WebhookResponse("0", message);
    }

    public static WebhookResponse failure(String message) {
        return new WebhookResponse("1", message);
    }

    public static WebhookResponse failure(String code, String message) {
        return new WebhookResponse(code, message);
    }

    public Map<String, String> toMap() {
        return Map.of(
                "ResultCode", resultCode,
                "ResultDesc", resultDesc
        );
    }

    @Override
    public String toString() {
        return String.format("WebhookResponse{code='%s', desc='%s'}", resultCode, resultDesc);
    }
}