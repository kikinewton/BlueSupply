package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum ApplicationUserPermission {
    EMPLOYEE_READ("employee:read"),
    EMPLOYEE_WRITE("employee:write"),
    HOD_READ("hod:read"),
    HOD_WRITE("hod:write"),
    PROCUREMENT_READ("procurement:read"),
    PROCUREMENT_WRITE("procurement:write"),
    GENERAL_MANAGER_READ("general_manager:read"),
    GENERAL_MANAGER_WRITE("general_manager:write"),
    ADMIN_READ("admin:read"),
    ADMIN_WRITE("admin:write");

    private String permission;

    ApplicationUserPermission(String permission) {
        this.permission = permission;
    }


}
