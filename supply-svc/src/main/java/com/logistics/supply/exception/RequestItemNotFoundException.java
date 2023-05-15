package com.logistics.supply.exception;

public class RequestItemNotFoundException extends NotFoundException {

    public RequestItemNotFoundException(String requestItemRef) {
        super("Request item with id: %s not found".formatted(requestItemRef));
    }

    public RequestItemNotFoundException(int requestItemId) {
        super("Request item with id: %s not found".formatted(requestItemId));
    }
}
