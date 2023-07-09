package com.logistics.supply.exception;

public class DepartmentNotFoundException extends NotFoundException {

    public DepartmentNotFoundException(String departmentName) {
        super("Department %s  not found");
    }

    public DepartmentNotFoundException(int departmentId) {
        super("Department with id: %s  not found");
    }
}
