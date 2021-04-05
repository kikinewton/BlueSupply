package com.logistics.supply.dto;

import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.EmployeeRole;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Getter
@ToString
public class RegistrationRequest {

    private String firstName;

    private String lastName;

    private String phoneNo;

    private List<EmployeeRole> employeeRole;

    private String email;

    private Department department;

}
