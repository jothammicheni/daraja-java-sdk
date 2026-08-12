package com.github.jothammicheni.daraja_springboot_starter_jdk.core.service;

import com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto.StkPushRequest;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.dto.StkPushResponse;

public interface MpesaClient {
    StkPushResponse initiateStkPush(StkPushRequest request);
    // future: B2C, balance, reversal, etc.
}