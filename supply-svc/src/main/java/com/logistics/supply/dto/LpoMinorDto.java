package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import com.logistics.supply.model.LocalPurchaseOrder;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
public class LpoMinorDto extends MinorDto {
  private EmployeeMinorDto approvedBy;
  private Boolean isApproved;
  private Set<RequestItemDto> requestItems;
  private QuotationMinorDto quotation;
  private String lpoRef;
  private Date deliveryDate;
  private Date createdAt;
  private DepartmentDto department;

  public static final LpoMinorDto toDto(LocalPurchaseOrder lpo) {
    LpoMinorDto lpoMinorDTO = new LpoMinorDto();
    BeanUtils.copyProperties(lpo, lpoMinorDTO);
    lpoMinorDTO.setId(lpo.getId());
    if (Objects.nonNull(lpo.getApprovedBy())) {
      EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(lpo.getApprovedBy());
      lpoMinorDTO.setApprovedBy(employeeMinorDTO);
    }
    if (Objects.nonNull(lpo.getQuotation())) {
      QuotationMinorDto quotationMinorDTO = QuotationMinorDto.toDto(lpo.getQuotation());
      lpoMinorDTO.setQuotation(quotationMinorDTO);
    }
    if (Objects.nonNull(lpo.getDepartment())) {
      DepartmentDto departmentDTO = DepartmentDto.toDto(lpo.getDepartment());
      lpoMinorDTO.setDepartment(departmentDTO);
    }
    if (lpo.getRequestItems() != null & !lpo.getRequestItems().isEmpty()) {
      Set<RequestItemDto> requestItemDTOS =
          lpo.getRequestItems().stream()
              .map(r -> RequestItemDto.toDto(r))
              .collect(Collectors.toSet());
      lpoMinorDTO.setRequestItems(requestItemDTOS);
    }
    return lpoMinorDTO;
  }

  public final static LpoMinorDto toDto2(LocalPurchaseOrder lpo) {
    LpoMinorDto lpoMinorDTO = new LpoMinorDto();
    BeanUtils.copyProperties(lpo, lpoMinorDTO);
    lpoMinorDTO.setId(lpo.getId());
    if (Objects.nonNull(lpo.getQuotation())) {
      QuotationMinorDto quotationMinorDTO = QuotationMinorDto.toDto(lpo.getQuotation());
      lpoMinorDTO.setQuotation(quotationMinorDTO);
    }
    if (lpo.getRequestItems() != null & !lpo.getRequestItems().isEmpty()) {
      Set<RequestItemDto> requestItemDTOS =
              lpo.getRequestItems().stream()
                      .map(r -> RequestItemDto.toDto(r))
                      .collect(Collectors.toSet());
      lpoMinorDTO.setRequestItems(requestItemDTOS);
    }
    return lpoMinorDTO;
  }
}
