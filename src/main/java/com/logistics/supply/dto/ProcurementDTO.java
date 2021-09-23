package com.logistics.supply.dto;

import com.logistics.supply.model.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ProcurementDTO {

  @NotBlank
  private BigDecimal unitPrice;
  @NotNull
  private Supplier supplier;

  public ProcurementDTO(BigDecimal unitPrice, Supplier supplier) {
    this.unitPrice = unitPrice;
    this.supplier = supplier;
  }
}
