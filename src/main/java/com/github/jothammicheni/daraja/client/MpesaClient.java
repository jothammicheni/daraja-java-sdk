package com.github.jothammicheni.daraja.client;

import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;

public interface MpesaClient {
    StkPushResponse initiateStkPush(StkPushRequest request);
}