package com.logistics.supply.enums;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Set;

@Getter
public enum ApplicationUserRole {
    ADMIN(Sets.newHashSet(ApplicationUserPermission.ADMIN_READ, ApplicationUserPermission.ADMIN_WRITE)),
    HOD(Sets.newHashSet(ApplicationUserPermission.HOD_READ, ApplicationUserPermission.HOD_WRITE)),
    PROCUREMENT_OFFICER(Sets.newHashSet(ApplicationUserPermission.PROCUREMENT_READ, ApplicationUserPermission.PROCUREMENT_WRITE)),
    GENERAL_MANAGER(Sets.newHashSet(ApplicationUserPermission.GENERAL_MANAGER_READ, ApplicationUserPermission.GENERAL_MANAGER_WRITE)),
    REGULAR(Sets.newHashSet(ApplicationUserPermission.EMPLOYEE_READ, ApplicationUserPermission.EMPLOYEE_WRITE));
    private Set<ApplicationUserPermission> permission;

    ApplicationUserRole(Set<ApplicationUserPermission> permission) {
        this.permission = permission;
    }
}
