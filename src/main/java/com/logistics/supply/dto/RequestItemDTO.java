package com.logistics.supply.dto;

import com.logistics.supply.enums.*;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RequestItemDTO extends MinorDTO {

  private String name;

  private RequestReason reason;

  private String purpose;

  private int quantity;

  private Date approvalDate;

  private Date createdDate;

  private PriorityLevel priorityLevel;

  private RequestStatus status = RequestStatus.PENDING;

  private RequestApproval approval = RequestApproval.PENDING;

  private EndorsementStatus endorsement = EndorsementStatus.PENDING;

  private RequestType requestType;

  private String currency;

  private Date requestDate;

  private BigDecimal unitPrice;

  private BigDecimal totalPrice;

  private String requestItemRef;

  private EmployeeMinorDTO employee;

  private Set<SupplierDTO> suppliers;

  public static RequestItemDTO toDto(RequestItem requestItem) {
    RequestItemDTO requestItemDTO = new RequestItemDTO();
    BeanUtils.copyProperties(requestItem, requestItemDTO);
    Employee employee1 = requestItem.getEmployee();
    if (employee1 != null) {
      EmployeeMinorDTO employeeMinorDTO = new EmployeeMinorDTO();
      BeanUtils.copyProperties(employee1, employeeMinorDTO);
      DepartmentDTO departmentDTO = new DepartmentDTO();
      BeanUtils.copyProperties(employee1.getDepartment(), departmentDTO);
      employee1.getRoles().stream().findAny().ifPresent(e -> employeeMinorDTO.setRole(e.getName()));
      employeeMinorDTO.setDepartment(departmentDTO);
      requestItemDTO.setEmployee(employeeMinorDTO);
    }
    if (!requestItem.getSuppliers().isEmpty()) {
      Set<SupplierDTO> suppliers =
          requestItem.getSuppliers().stream().map(SupplierDTO::toDto).collect(Collectors.toSet());
      requestItemDTO.setSuppliers(suppliers);
    }
    return requestItemDTO;
  }
}
