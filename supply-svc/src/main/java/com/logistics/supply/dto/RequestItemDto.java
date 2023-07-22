package com.logistics.supply.dto;

import com.logistics.supply.enums.*;
import com.logistics.supply.model.Department;
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
public class RequestItemDto extends MinorDto {

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

  private EmployeeMinorDto employee;

  private Set<SupplierDto> suppliers;

  private StoreDto receivingStore;

  private DepartmentDto userDepartment;


  public static RequestItemDto toDto(RequestItem requestItem) {
    RequestItemDto requestItemDTO = new RequestItemDto();
    BeanUtils.copyProperties(requestItem, requestItemDTO);
    Employee employee1 = requestItem.getEmployee();
    if (employee1 != null) {
      EmployeeMinorDto employeeMinorDTO = new EmployeeMinorDto();
      BeanUtils.copyProperties(employee1, employeeMinorDTO);
      DepartmentDto departmentDTO = new DepartmentDto();
      BeanUtils.copyProperties(employee1.getDepartment(), departmentDTO);
      employee1.getRoles().stream().findAny().ifPresent(e -> employeeMinorDTO.setRole(e.getName()));
      employeeMinorDTO.setDepartment(departmentDTO);
      requestItemDTO.setEmployee(employeeMinorDTO);
    }
    if (requestItem.getSuppliers() != null && !requestItem.getSuppliers().isEmpty()) {
      Set<SupplierDto> suppliers =
          requestItem.getSuppliers().stream().map(SupplierDto::toDto).collect(Collectors.toSet());
      requestItemDTO.setSuppliers(suppliers);
    }

    Department userDepartment = requestItem.getUserDepartment();
    if (null != userDepartment) {
      DepartmentDto departmentDto = DepartmentDto.toDto(userDepartment);
      requestItemDTO.setUserDepartment(departmentDto);
    }
    if (null != requestItem.getReceivingStore()) {
      StoreDto storeDto = new StoreDto();
      BeanUtils.copyProperties(requestItem.getReceivingStore(), storeDto);
      requestItemDTO.setReceivingStore(storeDto);
    }
    return requestItemDTO;
  }
}
