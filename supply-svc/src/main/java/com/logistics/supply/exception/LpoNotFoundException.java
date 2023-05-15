package com.logistics.supply.exception;

public class LpoNotFoundException extends NotFoundException {

    public LpoNotFoundException(String lpoRef) {
        super("Local purchase order with lpoRef: %s not found".formatted(lpoRef));
    }

    public LpoNotFoundException(int lpoId) {
        super("Local purchase order with id: %s not found".formatted(lpoId));
    }
}
