package com.logistics.supply.dto;

import com.logistics.supply.model.Department;
import com.logistics.supply.model.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import java.util.List;

@Getter
@Setter
@ToString
public class EmployeeDTO {

  List<Role> role;
  private String firstName;
  private String lastName;
  private String phoneNo;
  @Email private String email;
  private Department department;
  private String password;
}
