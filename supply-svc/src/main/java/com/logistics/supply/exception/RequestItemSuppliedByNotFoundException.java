package com.logistics.supply.exception;

public class RequestItemSuppliedByNotFoundException extends NotFoundException {

    public RequestItemSuppliedByNotFoundException(int requestItemId) {
        super("Final supplier for request item id: %s not assigned".formatted(requestItemId));
    }
}
