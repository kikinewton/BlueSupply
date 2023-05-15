package com.logistics.supply.exception;

public class RequestDocumentNotFoundException extends NotFoundException {
    public RequestDocumentNotFoundException(String requestDocumentRef) {
        super("Request document with id: %s not found".formatted(requestDocumentRef));
    }

    public RequestDocumentNotFoundException(int requestDocumentId) {
        super("Request document with id: %s not found".formatted(requestDocumentId));
    }
}
