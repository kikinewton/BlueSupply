package com.logistics.supply.fixture;

import com.logistics.supply.dto.EmployeeDto;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Role;

import java.util.List;

public class EmployeeDtoFixture {

  EmployeeDtoFixture() {}

  public static EmployeeDto getEmployeeDto() {

    EmployeeDto employeeDto = new EmployeeDto();
    employeeDto.setEmail("test@mail.com");
    employeeDto.setPassword("HardPass.(0)");
    employeeDto.setFirstName("Leo");
    employeeDto.setLastName("Turner");
    employeeDto.setPhoneNo("093983993");
    Role role = RoleFixture.getRole();
    employeeDto.setRole(List.of(role));
    Department department = DepartmentFixture.getDepartment("Finance");
    employeeDto.setDepartment(department);
    return employeeDto;
  }
}
