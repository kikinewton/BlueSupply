package com.logistics.supply.dto;

import com.logistics.supply.enums.*;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
    requestItemDTO.setId(requestItem.getId());
    requestItemDTO.setName(requestItem.getName());
    requestItemDTO.setReason(requestItem.getReason());
    requestItemDTO.setPurpose(requestItem.getPurpose());
    requestItemDTO.setQuantity(requestItem.getQuantity());
    requestItemDTO.setApprovalDate(requestItem.getApprovalDate());
    requestItemDTO.setCreatedDate(requestItem.getCreatedDate());
    requestItemDTO.setPriorityLevel(requestItem.getPriorityLevel());
    requestItemDTO.setStatus(requestItem.getStatus());
    requestItemDTO.setApproval(requestItem.getApproval());
    requestItemDTO.setEndorsement(requestItem.getEndorsement());
    requestItemDTO.setRequestType(requestItem.getRequestType());
    requestItemDTO.setCurrency(requestItem.getCurrency());
    requestItemDTO.setRequestDate(requestItem.getRequestDate());
    requestItemDTO.setUnitPrice(requestItem.getUnitPrice());
    requestItemDTO.setTotalPrice(requestItem.getTotalPrice());
    requestItemDTO.setRequestItemRef(requestItem.getRequestItemRef());
    Employee employee1 = requestItem.getEmployee();
    if (employee1 != null) {
      requestItemDTO.setEmployee(EmployeeMinorDto.toDto(employee1));
    }
    if (requestItem.getSuppliers() != null && !requestItem.getSuppliers().isEmpty()) {
      Set<SupplierDto> suppliers =
          requestItem.getSuppliers().stream().map(SupplierDto::toDto).collect(Collectors.toSet());
      requestItemDTO.setSuppliers(suppliers);
    }

    Department userDepartment = requestItem.getUserDepartment();
    if (null != userDepartment) {
      requestItemDTO.setUserDepartment(DepartmentDto.toDto(userDepartment));
    }
    if (null != requestItem.getReceivingStore()) {
      requestItemDTO.setReceivingStore(new StoreDto(requestItem.getReceivingStore().getName()));
    }
    return requestItemDTO;
  }
}
