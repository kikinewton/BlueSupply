package com.logistics.supply.enums;

public enum EmployeeLevel {
    REGULAR("REGULAR"), HOD("HOD"), SUPERVISOR("SUPERVISOR"), MANAGER("MANAGER"), GENERAL_MANAGER("GENERAL_MANAGER"), PROCUREMENT_OFFICER("PROCUREMENT-OFFICER");

    private String employeeLevel;

    private EmployeeLevel(String employeeLevel) {
        this.employeeLevel = employeeLevel;
    }

    public String getEmployeeLevel() {
        return employeeLevel;
    }
}
