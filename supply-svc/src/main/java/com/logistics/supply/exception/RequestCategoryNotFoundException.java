package com.logistics.supply.exception;

public class RequestCategoryNotFoundException extends NotFoundException {

    public RequestCategoryNotFoundException(int requestCategoryId) {
        super("Request category with id: %s not found".formatted(requestCategoryId));
    }
}
