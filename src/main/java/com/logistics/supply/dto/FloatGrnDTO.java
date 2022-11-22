package com.logistics.supply.dto;

import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.FloatOrder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class FloatGrnDTO extends MinorDTO {
  private boolean approvedByStoreManager;
  private Date dateOfApprovalByStoreManager;
  private Integer employeeStoreManager;
  private int floatOrderId;
  private FloatOrder.FloatOrderDTO floatOrder;
  private EmployeeMinorDTO createdBy;
  private Date createdDate;
  private Date updateDate;
  private Set<FloatDTO> receivedFloatItems;

  public static final FloatGrnDTO toDto(FloatGRN floatGRN) {
    FloatGrnDTO floatGrnDTO = new FloatGrnDTO();
    floatGrnDTO.setId((int) floatGRN.getId());
    BeanUtils.copyProperties(floatGRN, floatGrnDTO);
    if (floatGRN.getCreatedBy() != null) {
      EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(floatGRN.getCreatedBy());
      floatGrnDTO.setCreatedBy(employeeMinorDTO);
    }
    if (floatGRN.getFloats() != null && !floatGRN.getFloats().isEmpty()) {
      Set<FloatDTO> floatDTOS =
          floatGRN.getFloats().stream().map(FloatDTO::toDto).collect(Collectors.toSet());
      floatGrnDTO.setReceivedFloatItems(floatDTOS);
    }
    return floatGrnDTO;
  }
}
