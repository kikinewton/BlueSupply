package com.logistics.supply.exception;

public class QuotationNotFoundException extends NotFoundException {

    public QuotationNotFoundException(String quotationRef) {
        super("Quotation with quotationRef: %s not found".formatted(quotationRef));
    }

    public QuotationNotFoundException(int quotationId) {
        super("Quotation with quotation id: %s not found".formatted(quotationId));
    }
}
