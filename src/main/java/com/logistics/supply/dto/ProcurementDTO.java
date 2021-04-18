package com.logistics.supply.dto;

import com.logistics.supply.model.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ProcurementDTO {

  private BigDecimal unitPrice;
  private Supplier supplier;

  public ProcurementDTO(BigDecimal unitPrice, Supplier supplier) {
    this.unitPrice = unitPrice;
    this.supplier = supplier;
  }
}
