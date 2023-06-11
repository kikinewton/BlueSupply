package com.logistics.supply.exception;

public class RoleNotFoundException extends NotFoundException {

    public RoleNotFoundException(String roleName) {
        super("Role with name: %s not found".formatted(roleName));
    }
}
