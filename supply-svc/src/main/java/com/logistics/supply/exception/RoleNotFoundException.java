package com.logistics.supply.exception;

public class RoleNotFoundException extends NotFoundException {

    public RoleNotFoundException(String roleName) {
        super("Role with name: %s not found".formatted(roleName));
    }

    public RoleNotFoundException(int roleId) {
        super("Role with id: %s not found".formatted(roleId));
    }
}
