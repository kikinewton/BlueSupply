package com.logistics.supply.enums;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

import static com.logistics.supply.enums.ApplicationUserPermission.*;

@Getter
public enum ApplicationUserRole {
    ADMIN(Sets.newHashSet(ADMIN_READ, ADMIN_WRITE)),
    HOD(Sets.newHashSet(HOD_READ, HOD_WRITE)),
    PROCUREMENT_OFFICER(Sets.newHashSet(PROCUREMENT_READ, PROCUREMENT_WRITE)),
    GENERAL_MANAGER(Sets.newHashSet(GENERAL_MANAGER_READ, GENERAL_MANAGER_WRITE));

    private Set<ApplicationUserPermission> permission;

    ApplicationUserRole(Set<ApplicationUserPermission> permission) {
        this.permission = permission;
    }
}
