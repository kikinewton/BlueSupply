package com.logistics.supply.exception;

public class EmailAlreadyExistException extends BadRequestException {

    public EmailAlreadyExistException(String email) {
        super("Employee with email %s already exist".formatted(email), AppErrorCode.EMPLOYEE);
    }
}
