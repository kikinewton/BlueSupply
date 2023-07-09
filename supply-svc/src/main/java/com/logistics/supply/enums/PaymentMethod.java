package com.logistics.supply.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethod {
  CHEQUE("CHEQUE"),
  CASH("CASH"),
  MOBILE_MONEY("MOBILE-MONEY"),
  VISA("VISA"),
  BANK_TRANSFER("BANK-TRANSFER");

  private String paymentMethod;
}
