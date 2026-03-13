package com.logistics.supply.fixture;

import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Role;

import java.util.Collections;
import java.util.List;

public class RegistrationRequestFixture {

    RegistrationRequestFixture() {
    }

    public static RegistrationRequest getRegistrationRequest() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String firstName = "Leo";
        private String lastName = "Pops";
        private String phoneNo = "30949300293";
        private String email = "test@gmail.com";
        private List<Role> employeeRole = Collections.singletonList(RoleFixture.getRole());
        private Department department = DepartmentFixture.getDepartment("IT");

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder phoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder employeeRole(List<Role> employeeRole) {
            this.employeeRole = employeeRole;
            return this;
        }

        public Builder department(Department department) {
            this.department = department;
            return this;
        }

        public RegistrationRequest build() {
            RegistrationRequest request = new RegistrationRequest();
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setPhoneNo(phoneNo);
            request.setEmail(email);
            request.setEmployeeRole(employeeRole);
            request.setDepartment(department);
            return request;
        }
    }
}
