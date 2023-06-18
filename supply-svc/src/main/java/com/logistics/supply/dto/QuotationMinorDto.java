package com.logistics.supply.dto;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Quotation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

@Slf4j
@Setter
@Getter
@NoArgsConstructor
public class QuotationMinorDto extends MinorDto {
  private int id;
  private String fileName;
  private String quotationRef;
  private String supplier;
  private EmployeeMinorDto createdBy;
  private boolean reviewed;

  public static QuotationMinorDto toDto(Quotation quotation) {
    QuotationMinorDto quotationMinorDTO = new QuotationMinorDto();
    BeanUtils.copyProperties(quotation, quotationMinorDTO);
    quotationMinorDTO.setSupplier(quotation.getSupplier().getName());
    quotationMinorDTO.setFileName(quotation.getRequestDocument().getFileName());
    Employee createdBy1 = quotation.getCreatedBy();
    if (Objects.nonNull(createdBy1)) {
      EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(createdBy1);
      quotationMinorDTO.setCreatedBy(employeeMinorDTO);
    }
    return quotationMinorDTO;
  }
}
