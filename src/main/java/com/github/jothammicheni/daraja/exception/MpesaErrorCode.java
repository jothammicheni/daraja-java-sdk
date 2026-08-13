package com.github.jothammicheni.daraja_springboot_starter_jdk.core.exception;

public enum MpesaErrorCode {
    GENERIC_ERROR("MPESA-001", "An unexpected error occurred"),
    AUTH_FAILED("MPESA-002", "Authentication with M-Pesa failed"),
    INVALID_REQUEST("MPESA-003", "Invalid request payload"),
    WEBHOOK_SIGNATURE_INVALID("MPESA-004", "Webhook signature validation failed"),
    IDEMPOTENCY_VIOLATION("MPESA-005", "Duplicate request detected");

    private final String code;
    private final String defaultMessage;

    MpesaErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }
}
