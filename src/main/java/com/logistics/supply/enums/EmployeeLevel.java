package com.logistics.supply.enums;

public enum EmployeeLevel {
    REGULAR("REGULAR"), HOD("HOD"), GENERAL_MANAGER("GENERAL_MANAGER"), PROCUREMENT_OFFICER("PROCUREMENT_OFFICER"), ADMIN("ADMIN");

    private String employeeLevel;

    private EmployeeLevel(String employeeLevel) {
        this.employeeLevel = employeeLevel;
    }

    public String getEmployeeLevel() {
        return employeeLevel;
    }
}
