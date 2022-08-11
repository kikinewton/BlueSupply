package com.logistics.supply.dto;

import com.logistics.supply.model.LocalPurchaseOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
public class LpoMinorDTO extends MinorDTO {
  private EmployeeMinorDTO approvedBy;
  private Boolean isApproved;
  private Set<RequestItemDTO> requestItems;
  private QuotationMinorDTO quotation;
  private String lpoRef;
  private Date deliveryDate;
  private Date createdAt;
  private DepartmentDTO department;

  public static final LpoMinorDTO toDto(LocalPurchaseOrder lpo) {
    LpoMinorDTO lpoMinorDTO = new LpoMinorDTO();
    BeanUtils.copyProperties(lpo, lpoMinorDTO);
    lpoMinorDTO.setId(lpo.getId());
    if (Objects.nonNull(lpo.getApprovedBy())) {
      EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(lpo.getApprovedBy());
      lpoMinorDTO.setApprovedBy(employeeMinorDTO);
    }
    if (Objects.nonNull(lpo.getQuotation())) {
      QuotationMinorDTO quotationMinorDTO = QuotationMinorDTO.toDto(lpo.getQuotation());
      lpoMinorDTO.setQuotation(quotationMinorDTO);
    }
    if (Objects.nonNull(lpo.getDepartment())) {
      DepartmentDTO departmentDTO = DepartmentDTO.toDto(lpo.getDepartment());
      lpoMinorDTO.setDepartment(departmentDTO);
    }
    if (lpo.getRequestItems() != null & !lpo.getRequestItems().isEmpty()) {
      Set<RequestItemDTO> requestItemDTOS =
          lpo.getRequestItems().stream()
              .map(r -> RequestItemDTO.toDto(r))
              .collect(Collectors.toSet());
      lpoMinorDTO.setRequestItems(requestItemDTOS);
    }
    return lpoMinorDTO;
  }

  public final static LpoMinorDTO toDto2(LocalPurchaseOrder lpo) {
    LpoMinorDTO lpoMinorDTO = new LpoMinorDTO();
    BeanUtils.copyProperties(lpo, lpoMinorDTO);
    lpoMinorDTO.setId(lpo.getId());
    if (Objects.nonNull(lpo.getQuotation())) {
      QuotationMinorDTO quotationMinorDTO = QuotationMinorDTO.toDto(lpo.getQuotation());
      lpoMinorDTO.setQuotation(quotationMinorDTO);
    }
    if (lpo.getRequestItems() != null & !lpo.getRequestItems().isEmpty()) {
      Set<RequestItemDTO> requestItemDTOS =
              lpo.getRequestItems().stream()
                      .map(r -> RequestItemDTO.toDto(r))
                      .collect(Collectors.toSet());
      lpoMinorDTO.setRequestItems(requestItemDTOS);
    }
    return lpoMinorDTO;
  }
}
