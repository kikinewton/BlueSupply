package com.logistics.supply.exception;

public class ErrorException extends RuntimeException {

    private final AppErrorCode errorCode;

    public ErrorException(String message, AppErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode.getValue();
    }
}
