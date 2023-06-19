package com.logistics.supply.fixture;

import com.logistics.supply.model.Department;

public class DepartmentFixture {

     DepartmentFixture() {
    }

    public static Department getDepartment(String name) {
        Department department = new Department();
        department.setName(name);
        department.setId(1);
        return department;
    }
}
