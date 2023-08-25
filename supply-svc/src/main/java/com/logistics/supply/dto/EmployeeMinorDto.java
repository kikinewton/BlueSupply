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
public class EmployeeMinorDto extends MinorDto {
  private String firstName;
  private String lastName;
  private String phoneNo;
  @Email private String email;
  private String role;
  private DepartmentDto department;

  public static final EmployeeMinorDto toDto(Employee employee) {

    EmployeeMinorDto employeeMinorDTO = new EmployeeMinorDto();
    BeanUtils.copyProperties(employee, employeeMinorDTO);
    DepartmentDto departmentDto = new DepartmentDto();
    BeanUtils.copyProperties(employee.getDepartment(), departmentDto);
    employee.getRoles().stream().findAny().ifPresent(e -> employeeMinorDTO.setRole(e.getName()));
    employeeMinorDTO.setDepartment(departmentDto);
    return employeeMinorDTO;
  }

}
