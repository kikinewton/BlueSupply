package com.logistics.supply.exception;

public class RequestDocumentNotFoundException extends NotFoundException {
    public RequestDocumentNotFoundException(String fileName) {
        super("Request document with file name: %s not found".formatted(fileName));
    }

    public RequestDocumentNotFoundException(int requestDocumentId) {
        super("Request document with id: %s not found".formatted(requestDocumentId));
    }
}
