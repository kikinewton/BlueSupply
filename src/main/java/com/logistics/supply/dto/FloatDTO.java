package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class FloatDTO {
  private String itemDescription;
  private BigDecimal estimatedUnitPrice;
  private int quantity;
}
