package com.logistics.supply.fixture;

import com.logistics.supply.model.Role;

public class RoleFixture {

     RoleFixture() {
    }

    public static Role getRole() {
        Role role = new Role();
        role.setId(1);
        return role;
    }
}
