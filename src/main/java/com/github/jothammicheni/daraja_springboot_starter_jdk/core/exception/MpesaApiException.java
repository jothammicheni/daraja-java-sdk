package com.github.jothammicheni.daraja_springboot_starter_jdk.core.exception;

public class MpesaApiException extends RuntimeException {
    private final MpesaErrorCode errorCode;

    public MpesaApiException(String message) {
        super(message);
        this.errorCode = MpesaErrorCode.GENERIC_ERROR;
    }

    public MpesaApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = MpesaErrorCode.GENERIC_ERROR;
    }

    public MpesaApiException(MpesaErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MpesaErrorCode getErrorCode() { return errorCode; }
}
