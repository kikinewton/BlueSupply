package com.logistics.supply.dto;

import com.logistics.supply.model.Supplier;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProcurementDTO {

  private BigDecimal unitPrice;
  private Supplier supplier;

}
