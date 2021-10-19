package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidName;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.Role;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
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
