package com.logistics.supply.dto;

import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.model.Floats;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class FloatOrderDto extends MinorDto {

    private String staffId;
    private BigDecimal amount;
    private String description;
    private String floatOrderRef;
    private LocalDate createdDate;
    private boolean fundsReceived;
    private Date retirementDate;
    private DepartmentDto department;
    private RequestApproval approval;
    private RequestStatus status;
    private EmployeeMinorDto createdBy;
    private Set<FloatDto> floats;

    public static FloatOrderDto toDto(FloatOrder floatOrder) {
      FloatOrderDto floatOrderDTO = new FloatOrderDto();
      BeanUtils.copyProperties(floatOrder, floatOrderDTO);
      DepartmentDto departmentDTO = DepartmentDto.toDto(floatOrder.getDepartment());
      floatOrderDTO.setDepartment(departmentDTO);
      EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(floatOrder.getCreatedBy());
      floatOrderDTO.setCreatedBy(employeeMinorDTO);
      Set<Floats> floats1 = floatOrder.getFloats();
      if (floats1 != null && !floats1.isEmpty()) {
        Set<FloatDto> floatDTOS =
            floats1.stream().map(f -> FloatDto.toDto(f)).collect(Collectors.toSet());
        floatOrderDTO.setFloats(floatDTOS);
      }
      return floatOrderDTO;
    }

}
