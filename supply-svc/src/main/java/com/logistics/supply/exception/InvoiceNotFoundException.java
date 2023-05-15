package com.logistics.supply.exception;

public class InvoiceNotFoundException extends NotFoundException {

    public InvoiceNotFoundException(String invoiceRef) {
        super("Invoice with ref: %s not found".formatted(invoiceRef));
    }

    public InvoiceNotFoundException(int invoiceId) {
        super("Invoice with id: %s not found".formatted(invoiceId));
    }
}
