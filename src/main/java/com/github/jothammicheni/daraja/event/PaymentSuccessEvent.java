package com.github.jothammicheni.daraja.event;

import com.github.jothammicheni.daraja.dto.webhook.WebhookPayload;

/**
 * Fired when a payment is successfully completed.
 * Contains all transaction details including receipt, amount, and phone.
 */
public class PaymentSuccessEvent extends MpesaEvent {

    private final String transactionId;
    private final String receiptNumber;
    private final String amount;
    private final String phoneNumber;
    private final String maskedPhoneNumber;
    private final String accountReference;
    private final String transactionDate;

    public PaymentSuccessEvent(WebhookPayload payload) {
        super(payload.getCheckoutRequestId(),
                payload.getMerchantRequestId(),
                EventType.PAYMENT_SUCCESS);
        this.transactionId = payload.getTransactionId();
        this.receiptNumber = payload.getReceiptNumber();
        this.amount = payload.getAmount();
        this.phoneNumber = payload.getPhoneNumber();
        this.maskedPhoneNumber = payload.getMaskedPhoneNumber();
        this.accountReference = payload.getAccountReference();
        this.transactionDate = payload.getTransactionDate();
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getReceiptNumber() { return receiptNumber; }
    public String getAmount() { return amount; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMaskedPhoneNumber() { return maskedPhoneNumber; }
    public String getAccountReference() { return accountReference; }
    public String getTransactionDate() { return transactionDate; }

    public boolean hasReceipt() {
        return receiptNumber != null && !receiptNumber.isBlank();
    }

    @Override
    public String toString() {
        return String.format("PaymentSuccessEvent{checkoutId='%s', receipt='%s', amount='%s', phone='%s'}",
                getCheckoutId(), receiptNumber, amount, maskedPhoneNumber);
    }
}