package com.logistics.supply.exception;

public class EmployeeNotEnabledException extends BadRequestException {

    public EmployeeNotEnabledException(String email) {
        super("Employee with email: {} not enabled".formatted(email), AppErrorCode.EMPLOYEE);
    }
}
