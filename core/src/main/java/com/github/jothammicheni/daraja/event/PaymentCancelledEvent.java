package com.github.jothammicheni.daraja.event;

import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;

/**
 * Fired when a user cancels the payment from their phone.
 */
public class PaymentCancelledEvent extends MpesaEvent {

    private final String resultCode;
    private final String resultDescription;
    private final String phoneNumber;
    private final String maskedPhoneNumber;
    private final String accountReference;

    public PaymentCancelledEvent(WebhookPayload payload) {
        super(payload.getCheckoutRequestId(),
                payload.getMerchantRequestId(),
                EventType.PAYMENT_CANCELLED);

        this.resultCode = payload.getResultCode();
        this.resultDescription = payload.getResultDescription();
        this.phoneNumber = payload.getPhoneNumber();
        this.maskedPhoneNumber = payload.getMaskedPhoneNumber();
        this.accountReference = payload.getAccountReference();
    }

    // Getters
    public String getResultCode() { return resultCode; }
    public String getResultDescription() { return resultDescription; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMaskedPhoneNumber() { return maskedPhoneNumber; }
    public String getAccountReference() { return accountReference; }

    @Override
    public String toString() {
        return String.format("PaymentCancelledEvent{checkoutId='%s', phone='%s'}",
                getCheckoutId(), maskedPhoneNumber);
    }
}