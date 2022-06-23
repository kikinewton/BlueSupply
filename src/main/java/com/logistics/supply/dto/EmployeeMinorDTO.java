package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeMinorDTO {
    private String firstName;
    private String lastName;
    private String phoneNo;
    @Email
    private String email;
    private DepartmentDTO department;
}
