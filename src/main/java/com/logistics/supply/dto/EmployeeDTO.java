package com.logistics.supply.dto;

import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.model.Department;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class EmployeeDTO {

    private String firstName;

    private String lastName;

    private String phoneNo;

    private String email;

    private Department department;

    private String password;


}
