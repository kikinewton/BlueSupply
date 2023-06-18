package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import com.logistics.supply.model.LocalPurchaseOrderDraft;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
public class LpoDraftDto extends MinorDto {
    private Set<RequestItemDto> requestItems;
    private QuotationMinorDto quotation;
    private Date deliveryDate;
    private Date createdAt;
    private DepartmentDto department;
    private Integer supplierId;

    public static final LpoDraftDto toDto(LocalPurchaseOrderDraft lpo) {
        LpoDraftDto lpoDraftDTO = new LpoDraftDto();
        BeanUtils.copyProperties(lpo, lpoDraftDTO);
        lpoDraftDTO.setId(lpo.getId());
//        if (lpo.getCreatedBy().isPresent()) {
//      EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(lpo.getCreatedBy().get());
//            lpoDraftDTO.setApprovedBy(employeeMinorDTO);
//        }
        if (Objects.nonNull(lpo.getQuotation())) {
            QuotationMinorDto quotationMinorDTO = QuotationMinorDto.toDto(lpo.getQuotation());
            lpoDraftDTO.setQuotation(quotationMinorDTO);
        }
        if (Objects.nonNull(lpo.getDepartment())) {
            DepartmentDto departmentDTO = DepartmentDto.toDto(lpo.getDepartment());
            lpoDraftDTO.setDepartment(departmentDTO);
        }
        if (lpo.getRequestItems() != null & !lpo.getRequestItems().isEmpty()) {
            Set<RequestItemDto> requestItemDTOS =
                    lpo.getRequestItems().stream()
                            .map(RequestItemDto::toDto)
                            .collect(Collectors.toSet());
            lpoDraftDTO.setRequestItems(requestItemDTOS);
        }
        return lpoDraftDTO;
    }
}
