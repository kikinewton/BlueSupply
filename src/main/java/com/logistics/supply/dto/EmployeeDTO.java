package com.logistics.supply.dto;

import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.EmployeeRole;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Email;
import java.util.List;


@Getter
@ToString
public class EmployeeDTO {

    private String firstName;

    private String lastName;

    private String phoneNo;

    @Email
    private String email;

    private Department department;

    private String password;

    List<EmployeeRole> role;


}
