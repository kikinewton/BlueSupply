package com.logistics.supply.exception;

public class FloatGrnNotFoundException extends NotFoundException {
    public FloatGrnNotFoundException(String floatGrnRef) {
        super("Float GRN with ref: %s not found".formatted(floatGrnRef));
    }

    public FloatGrnNotFoundException(int floatGrnId) {
        super("Float GRN with id: %s not found".formatted(floatGrnId));
    }
}
