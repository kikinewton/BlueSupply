package com.logistics.supply.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
  PENDING("PENDING"),
  PARTIAL("PARTIAL"),
  COMPLETED("COMPLETED"),
  DISPUTED("DISPUTED");

  private String paymentStatus;
}
