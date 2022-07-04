package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum RequestProcess {
  HOD_REQUEST_ENDORSEMENT("HOD_REQUEST_ENDORSEMENT"),
  PRICE_DETAILS_PROCUREMENT("PRICE_DETAILS_PROCUREMENT"),
  PRICE_REVIEW_HOD("PRICE_REVIEW_HOD"),
  REQUEST_APPROVAL_GM("REQUEST_APPROVAL_GM"),
  LPO_PROCUREMENT("LPO_PROCUREMENT"),
  GRN_STORES("GRN_STORES"),
  REVIEW_GRN_HOD("REVIEW_GRN_HOD"),
  REVIEW_GRN_PROCUREMENT("REVIEW_GRN_PROCUREMENT"),
  REVIEW_PAYMENT_DRAFT_GM("REVIEW_PAYMENT_DRAFT_GM"),
  REVIEW_PAYMENT_DRAFT_FM("REVIEW_PAYMENT_DRAFT_FM"),
  REVIEW_PAYMENT_DRAFT_AUDITOR("REVIEW_PAYMENT_DRAFT_AUDITOR"),
  ACCOUNT_OFFICER_RESPONSE_TO_AUDITOR_COMMENT("RESPONSE_TO_AUDITOR_COMMENT"),
  REVIEW_GRN_ACCOUNTS("REVIEW_GRN_ACCOUNTS"),
  REVIEW_PETTY_CASH("REVIEW_PETTY_CASH");


  String process;

  RequestProcess(String process) {
    this.process = process;
  }
}
