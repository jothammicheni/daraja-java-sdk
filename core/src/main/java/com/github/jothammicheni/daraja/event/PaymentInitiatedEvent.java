package com.github.jothammicheni.daraja.event;

import com.github.jothammicheni.daraja.dto.StkPushRequest;
import com.github.jothammicheni.daraja.dto.StkPushResponse;

/**
 * Fired when an STK Push request is successfully initiated.
 */
public class PaymentInitiatedEvent extends MpesaEvent {

    private final String checkoutRequestId;
    private final String merchantRequestId;
    private final String phoneNumber;
    private final String maskedPhoneNumber;
    private final int amount;
    private final String accountReference;
    private final String idempotencyKey;

    public PaymentInitiatedEvent(StkPushRequest request, StkPushResponse response) {
        super(response.checkoutRequestID(),
                response.merchantRequestID(),
                EventType.PAYMENT_INITIATED);

        this.checkoutRequestId = response.checkoutRequestID();
        this.merchantRequestId = response.merchantRequestID();
        this.phoneNumber = request.phoneNumber();
        this.maskedPhoneNumber = request.getMaskedPhoneNumber();
        this.amount = request.amount();
        this.accountReference = request.accountReference();
        this.idempotencyKey = request.idempotencyKey();
    }

    // Getters
    public String getCheckoutRequestId() { return checkoutRequestId; }
    public String getMerchantRequestId() { return merchantRequestId; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMaskedPhoneNumber() { return maskedPhoneNumber; }
    public int getAmount() { return amount; }
    public String getAccountReference() { return accountReference; }
    public String getIdempotencyKey() { return idempotencyKey; }

    @Override
    public String toString() {
        return String.format("PaymentInitiatedEvent{checkoutId='%s', amount='%s', phone='%s'}",
                getCheckoutId(), amount, maskedPhoneNumber);
    }
}