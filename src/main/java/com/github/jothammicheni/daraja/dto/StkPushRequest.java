package com.github.jothammicheni.daraja.dto;

import java.io.Serializable;

public record StkPushRequest(
        String businessShortCode,
        String phoneNumber,
        int amount,
        String partyA,
        String partyB,
        String accountReference,
        String transactionDesc,
        String idempotencyKey
) implements Serializable {
    private static final long serialVersionUID = 1L;
}