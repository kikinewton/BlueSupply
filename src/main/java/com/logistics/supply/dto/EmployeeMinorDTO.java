package com.logistics.supply.dto;

import com.logistics.supply.model.Employee;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.Email;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeMinorDTO extends MinorDTO {
  private String firstName;
  private String lastName;
  private String phoneNo;
  @Email private String email;
  private String role;
  private DepartmentDTO department;

  public static final EmployeeMinorDTO toDto(Employee employee) {
    EmployeeMinorDTO employeeMinorDTO = new EmployeeMinorDTO();
    BeanUtils.copyProperties(employee, employeeMinorDTO);
    DepartmentDTO departmentDTO = DepartmentDTO.toDto(employee.getDepartment());
    employeeMinorDTO.setDepartment(departmentDTO);
    employee.getRoles().stream().findAny().ifPresent(e -> employeeMinorDTO.setRole(e.getName()));
    return employeeMinorDTO;
  }
}
