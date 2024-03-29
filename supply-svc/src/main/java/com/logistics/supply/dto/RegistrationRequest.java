package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidName;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
@ToString
public class RegistrationRequest {

    @NotBlank
    @ValidName(message = "Invalid first name")
    private String firstName;

    @NotBlank
    @ValidName(message = "Invalid lastname")
    private String lastName;

    @NotBlank(message = "Phone number can not be blank")
    private String phoneNo;

    @Size(max = 1, min = 1)
    @NotEmpty
    private List<Role> employeeRole;

    @Email
    private String email;

     @NotNull
    private Department department;

}
