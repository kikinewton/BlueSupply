package com.logistics.supply.exception;

public class FloatsNotFoundException extends NotFoundException {
    public FloatsNotFoundException(String floatRef) {
        super("Float with floatRef: %s not found".formatted(floatRef));
    }

    public FloatsNotFoundException(int floatId) {
        super("Float with floatId: %s not found".formatted(floatId));
    }

}
