package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

@Slf4j
@Setter
@Getter
@NoArgsConstructor
public class QuotationMinorDTO extends MinorDTO {
  private int id;
  private String fileName;
  private String quotationRef;
  private String supplier;

  public static QuotationMinorDTO toDto(Quotation quotation) {
    QuotationMinorDTO quotationMinorDTO = new QuotationMinorDTO();
    BeanUtils.copyProperties(quotation, quotationMinorDTO);
    quotationMinorDTO.setSupplier(quotation.getSupplier().getName());
    quotationMinorDTO.setFileName(quotation.getRequestDocument().getFileName());
    return quotationMinorDTO;
  }
}
