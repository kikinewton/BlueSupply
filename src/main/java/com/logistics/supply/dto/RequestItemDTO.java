package com.logistics.supply.dto;

import com.logistics.supply.enums.PriorityLevel;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RequestItemDTO {
  private int id;
  private String name;

  private RequestReason reason;

  private String purpose;

  private int quantity;

  private PriorityLevel priorityLevel;

  private EmployeeMinorDTO employee;

  public static RequestItemDTO toDto(RequestItem requestItem) {
    RequestItemDTO requestItemDTO = new RequestItemDTO();
    BeanUtils.copyProperties(requestItem, requestItemDTO);
    Employee employee1 = requestItem.getEmployee();
    if (employee1 != null) {
      EmployeeMinorDTO employeeMinorDTO = new EmployeeMinorDTO();
      BeanUtils.copyProperties(employee1, employeeMinorDTO);
      DepartmentDTO departmentDTO = new DepartmentDTO();
      BeanUtils.copyProperties( employee1.getDepartment(), departmentDTO);
      employeeMinorDTO.setDepartment(departmentDTO);
      requestItemDTO.setEmployee(employeeMinorDTO);
    }
    return requestItemDTO;
  }
}
