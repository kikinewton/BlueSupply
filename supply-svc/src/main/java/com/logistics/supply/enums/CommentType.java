package com.logistics.supply.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentType {
  LPO_COMMENT("LPO_COMMENT"),
  FLOAT_COMMENT("FLOAT_COMMENT"),
  PETTY_CASH_COMMENT("PETTY_CASH_COMMENT"),
  QUOTATION_COMMENT("QUOTATION_COMMENT"),
  STORES_COMMENT("STORES_COMMENT"),
  GRN_COMMENT("GM_PAYMENT_COMMENT"),
  PAYMENT_COMMENT("ACCOUNT_PAYMENT_COMMENT"),
  FLOAT_GRN_COMMENT("FLOAT_GRN_COMMENT");

  private String commentType;
}
