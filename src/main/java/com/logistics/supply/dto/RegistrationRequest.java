package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidName;
import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.EmployeeRole;
import lombok.Getter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;
import java.util.Set;

@Getter
@ToString
public class RegistrationRequest {

    @NotBlank
    @ValidName
    private String firstName;

    @NotBlank
    @ValidName
    private String lastName;

    @NotBlank
    private String phoneNo;

    @Size(max = 1, min = 1)
    @NotEmpty
    private List<EmployeeRole> employeeRole;

    @Email
    private String email;

    @Valid @NotNull
    private Department department;

}
