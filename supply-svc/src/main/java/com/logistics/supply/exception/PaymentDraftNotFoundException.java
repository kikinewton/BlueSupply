package com.logistics.supply.exception;

public class PaymentDraftNotFoundException extends NotFoundException {

    public PaymentDraftNotFoundException(int paymentDraftId) {
        super("Payment draft with id: %s not found".formatted(paymentDraftId));
    }
}
