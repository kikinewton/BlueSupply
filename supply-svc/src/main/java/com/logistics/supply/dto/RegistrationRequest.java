package com.logistics.supply.dto;

import com.logistics.supply.model.Department;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.*;
import com.logistics.supply.annotation.ValidName;
import com.logistics.supply.model.Role;
import java.util.List;

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
    private List<Role> employeeRole;

    @Email
    private String email;

     @NotNull
    private Department department;

}
