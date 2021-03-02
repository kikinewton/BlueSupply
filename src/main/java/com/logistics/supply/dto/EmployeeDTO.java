package com.logistics.supply.dto;

import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.model.Department;
import lombok.Data;
import lombok.Getter;


@Getter
public class EmployeeDTO {

    private String firstName;

    private String lastName;

    private String phoneNo;

    private Boolean enabled;

    private EmployeeLevel employeeLevel;

    private String email;

    private Department department;


}
