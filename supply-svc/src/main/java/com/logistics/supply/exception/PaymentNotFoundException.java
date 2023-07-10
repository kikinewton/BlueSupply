package com.logistics.supply.exception;

public class PaymentNotFoundException extends NotFoundException {
    public PaymentNotFoundException(int paymentId) {
        super("Payment with id %s not found".formatted(paymentId));
    }
}
