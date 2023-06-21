package com.logistics.supply.dto;

import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.PettyCashOrder;
import com.logistics.supply.model.RequestDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class PettyCashOrderDto extends MinorDto {

    private String staffId;
    private RequestStatus status;
    private EmployeeMinorDto createdBy;
    private String pettyCashOrderRef;
    private Set<ItemDto> pettyCash;
    private List<RequestDocument> supportingDocument;

    public static PettyCashOrder.PettyCashOrderDto toDto(PettyCashOrder pettyCashOrder) {
        PettyCashOrder.PettyCashOrderDto pettyCashOrderDTO = new PettyCashOrder.PettyCashOrderDto();
        BeanUtils.copyProperties(pettyCashOrder, pettyCashOrderDTO);
        if(pettyCashOrder.getCreatedBy().isPresent()){
            EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(pettyCashOrder.getCreatedBy().get());
            pettyCashOrderDTO.setCreatedBy(employeeMinorDTO);
        }
        if(pettyCashOrder.getPettyCash() !=  null && !pettyCashOrder.getPettyCash().isEmpty()) {
            pettyCashOrder.getPettyCash().forEach(f -> {
                ItemDto itemDTO = new ItemDto();
                BeanUtils.copyProperties(f, itemDTO);
                itemDTO.setUnitPrice(f.getAmount());
            });
        }
        if (pettyCashOrder.getSupportingDocument() != null && !pettyCashOrder.getSupportingDocument().isEmpty()) {
            pettyCashOrderDTO.setSupportingDocument(pettyCashOrder.getSupportingDocument());
        }
        return pettyCashOrderDTO;
    }

}
