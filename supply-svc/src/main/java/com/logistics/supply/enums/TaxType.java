package com.logistics.supply.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaxType {
  SERVICE("SERVICE"),
  GOODS("GOODS");
  private String taxType;
}
