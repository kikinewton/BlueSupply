package com.logistics.supply.exception;

public class UnauthorizedUpdateException extends BadRequestException {
    public UnauthorizedUpdateException(String message) {
        super(message, AppErrorCode.EMPLOYEE);
    }
}
