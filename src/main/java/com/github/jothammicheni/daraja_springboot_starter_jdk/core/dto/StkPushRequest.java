package com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

public record StkPushRequest(
        @NotBlank String businessShortCode,
        @NotBlank @Size(min = 10, max = 13) String phoneNumber,
        @NotNull @Min(1) Integer amount, // ⚡ FIXED: Changed from String to Integer to support @Min validation safely
        @NotBlank String partyA,
        @NotBlank String partyB,
        @NotBlank String accountReference,
        @NotBlank String transactionDesc,
        @NotBlank String idempotencyKey
) implements Serializable { // ⚡ FIXED: Added Serializable implementation for robust AWS distributed caching

    private static final long serialVersionUID = 1L;
}
