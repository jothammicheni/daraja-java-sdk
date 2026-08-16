package com.github.jothammicheni.daraja.event;

import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;

/**
 * Fired when a payment fails.
 * Contains the failure reason and result code.
 */
public class PaymentFailedEvent extends MpesaEvent {

    private final String resultCode;
    private final String resultDescription;
    private final String phoneNumber;
    private final String maskedPhoneNumber;
    private final String accountReference;

    public PaymentFailedEvent(WebhookPayload payload) {
        super(payload.getCheckoutRequestId(),
                payload.getMerchantRequestId(),
                EventType.PAYMENT_FAILED);

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

    public boolean isUserCancelled() {
        return "1032".equals(resultCode);
    }

    public boolean isInsufficientFunds() {
        return "2001".equals(resultCode);
    }

    public boolean isTimeout() {
        return "1020".equals(resultCode);
    }

    @Override
    public String toString() {
        return String.format("PaymentFailedEvent{checkoutId='%s', code='%s', reason='%s', phone='%s'}",
                getCheckoutId(), resultCode, resultDescription, maskedPhoneNumber);
    }
}