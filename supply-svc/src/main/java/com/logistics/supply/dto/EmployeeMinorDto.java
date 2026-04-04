package com.logistics.supply.dto;

import com.logistics.supply.model.Employee;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeMinorDto extends MinorDto {
  private String firstName;
  private String lastName;
  private String phoneNo;
  @Email private String email;
  private String role;
  private DepartmentDto department;

  public static EmployeeMinorDto toDto(Employee employee) {
    EmployeeMinorDto employeeMinorDTO = new EmployeeMinorDto();
    employeeMinorDTO.setId(employee.getId());
    employeeMinorDTO.setFirstName(employee.getFirstName());
    employeeMinorDTO.setLastName(employee.getLastName());
    employeeMinorDTO.setPhoneNo(employee.getPhoneNo());
    employeeMinorDTO.setEmail(employee.getEmail());
    employee.getRoles().stream().findAny().ifPresent(e -> employeeMinorDTO.setRole(e.getName()));
    employeeMinorDTO.setDepartment(DepartmentDto.toDto(employee.getDepartment()));
    return employeeMinorDTO;
  }

}
