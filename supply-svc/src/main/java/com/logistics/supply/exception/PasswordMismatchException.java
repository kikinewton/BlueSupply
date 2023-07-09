package com.logistics.supply.exception;

public class PasswordMismatchException extends BadRequestException {

    public PasswordMismatchException() {
        super("Password does not match the stored hash", AppErrorCode.EMPLOYEE);
    }
}
