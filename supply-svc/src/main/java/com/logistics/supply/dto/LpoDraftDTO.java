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
public class LpoDraftDTO extends MinorDTO {
    private Set<RequestItemDTO> requestItems;
    private QuotationMinorDTO quotation;
    private Date deliveryDate;
    private Date createdAt;
    private DepartmentDTO department;
    private Integer supplierId;

    public static final LpoDraftDTO toDto(LocalPurchaseOrderDraft lpo) {
        LpoDraftDTO lpoDraftDTO = new LpoDraftDTO();
        BeanUtils.copyProperties(lpo, lpoDraftDTO);
        lpoDraftDTO.setId(lpo.getId());
//        if (lpo.getCreatedBy().isPresent()) {
//      EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(lpo.getCreatedBy().get());
//            lpoDraftDTO.setApprovedBy(employeeMinorDTO);
//        }
        if (Objects.nonNull(lpo.getQuotation())) {
            QuotationMinorDTO quotationMinorDTO = QuotationMinorDTO.toDto(lpo.getQuotation());
            lpoDraftDTO.setQuotation(quotationMinorDTO);
        }
        if (Objects.nonNull(lpo.getDepartment())) {
            DepartmentDTO departmentDTO = DepartmentDTO.toDto(lpo.getDepartment());
            lpoDraftDTO.setDepartment(departmentDTO);
        }
        if (lpo.getRequestItems() != null & !lpo.getRequestItems().isEmpty()) {
            Set<RequestItemDTO> requestItemDTOS =
                    lpo.getRequestItems().stream()
                            .map(RequestItemDTO::toDto)
                            .collect(Collectors.toSet());
            lpoDraftDTO.setRequestItems(requestItemDTOS);
        }
        return lpoDraftDTO;
    }
}
