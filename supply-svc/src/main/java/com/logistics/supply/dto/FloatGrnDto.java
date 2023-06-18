package com.logistics.supply.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.FloatOrder;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class FloatGrnDto extends MinorDto {
  private boolean approvedByStoreManager;
  private Date dateOfApprovalByStoreManager;
  private Integer employeeStoreManager;
  private int floatOrderId;
  private FloatOrder.FloatOrderDto floatOrder;
  private EmployeeMinorDto createdBy;
  private Date createdDate;
  private Date updateDate;
  private Set<FloatDto> receivedFloatItems;

  public static final FloatGrnDto toDto(FloatGRN floatGRN) {
    FloatGrnDto floatGrnDTO = new FloatGrnDto();
    floatGrnDTO.setId((int) floatGRN.getId());
    BeanUtils.copyProperties(floatGRN, floatGrnDTO);
    if (floatGRN.getCreatedBy() != null) {
      EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(floatGRN.getCreatedBy());
      floatGrnDTO.setCreatedBy(employeeMinorDTO);
    }
    if (floatGRN.getFloats() != null && !floatGRN.getFloats().isEmpty()) {
      Set<FloatDto> floatDTOS =
          floatGRN.getFloats().stream().map(FloatDto::toDto).collect(Collectors.toSet());
      floatGrnDTO.setReceivedFloatItems(floatDTOS);
    }
    return floatGrnDTO;
  }
}
