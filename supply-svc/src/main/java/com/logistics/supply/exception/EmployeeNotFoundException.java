package com.logistics.supply.exception;

public class EmployeeNotFoundException extends NotFoundException {


    public EmployeeNotFoundException(String email) {
        super("Employee with email: %s not found".formatted(email));
    }

    public EmployeeNotFoundException(long employeeId) {
        super("Employee with id: %s not found".formatted(employeeId));
    }
}
