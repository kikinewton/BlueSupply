package com.logistics.supply.dto;

import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.model.Department;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RegistrationRequest {

    private String firstName;

    private String lastName;

    private String phoneNo;

    private EmployeeLevel employeeLevel;

    private String email;

    private Department department;

}
