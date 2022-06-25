package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class FloatDTO extends MinorDTO{
  private String itemDescription;
  private BigDecimal estimatedUnitPrice;
  private int quantity;
}
