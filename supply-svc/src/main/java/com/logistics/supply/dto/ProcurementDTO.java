package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.index.qual.Positive;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.logistics.supply.annotation.ValidEndorsed;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ProcurementDTO {
  @NotNull @ValidEndorsed
  private RequestItem requestItem;
  @NotBlank @Positive
  private BigDecimal unitPrice;
  @NotNull
  private Supplier supplier;

  public ProcurementDTO(BigDecimal unitPrice, Supplier supplier) {
    this.unitPrice = unitPrice;
    this.supplier = supplier;
  }
}
