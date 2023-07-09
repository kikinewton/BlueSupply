package com.logistics.supply.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProcurementType {
  LPO("LPO"),
  FLOAT("FLOAT"),
  PETTY_CASH("PETTY_CASH");
  String procurementType;
}
