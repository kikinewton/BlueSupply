package com.logistics.supply.exception;

public class RetireFloatOrderException extends BadRequestException {

    public RetireFloatOrderException(String message) {
        super(message, AppErrorCode.FLOAT_REQUEST);
    }
}
