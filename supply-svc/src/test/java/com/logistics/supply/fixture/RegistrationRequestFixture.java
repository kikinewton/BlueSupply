package com.logistics.supply.fixture;

import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Role;

import java.util.Collections;

public class RegistrationRequestFixture {

     RegistrationRequestFixture() {
    }

    public static RegistrationRequest getRegistrationRequest() {
        Department department = DepartmentFixture.getDepartment("IT");
        Role role = RoleFixture.getRole();
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("test@gmail.com");
        registrationRequest.setPhoneNo("30949300293");
        registrationRequest.setLastName("Pops");
        registrationRequest.setFirstName("Leo");
        registrationRequest.setDepartment(department);
        registrationRequest.setEmployeeRole(Collections.singletonList(role));
        return registrationRequest;
    }
}
