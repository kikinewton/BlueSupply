package com.logistics.supply.exception;

public class BadRequestException extends ErrorException {

    public BadRequestException(String message, AppErrorCode errorCode) {
        super(message, errorCode);
    }
}
