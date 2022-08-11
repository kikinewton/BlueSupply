package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum EmailType {
  NEW_REQUEST("NEW REQUEST"),
  QUOTATION_TO_GM_AND_HOD_MAIL("QUOTATIONS FROM SUPPLIERS"),
  NOTIFY_EMPLOYEE_OF_ENDORSEMENT_MAIL("HOD ENDORSEMENT"),
  REQUEST_ENDORSEMENT_MAIL("ENDORSEMENT OF REQUEST"),
  PROCUREMENT_REVIEW_MAIL("PROCUREMENT DETAILS FOR REQUEST"),
  GENERAL_MANAGER_APPROVAL_MAIL("REQUEST APPROVAL"),
  CANCELLED_REQUEST_MAIL("CANCELLED REQUEST"),
  NEW_USER_PASSWORD_MAIL("PASSWORD RESET"),
  NEW_USER_CONFIRMATION_MAIL("CONFIRMATION"),
  STORES_GOODS_RECEIVED_EMAIL("GOODS RECEIVED FROM SUPPLIER"),
  SUPPLIER_QUOTATION_EMAIL(""),
  LPO_TO_STORES_EMAIL("LPO TO STORES"),
  APPROVED_REQUEST_MAIL("APPROVED REQUEST"),
  PAYMENT_DUE_EMAIL(""),
  FLOAT_ENDORSEMENT_EMAIL("FLOAT ENDORSEMENT"),
  PETTY_CASH_ENDORSEMENT_EMAIL("PETTY CASH ENDORSEMENT"),
  PETTY_CASH_APPROVAL_EMAIL("PETTY CASH APPROVAL"),
  FLOAT_ENDORSED_EMAIL_TO_EMPLOYEE("FLOAT ENDORSED"),
  PETTY_CASH_ENDORSED_EMAIL_TO_EMPLOYEE("PETTY CASH ENDORSED"),
  REQUEST_ITEM_COMMENT_EMAIL_TO_EMPLOYEE("COMMENT ON REQUEST ITEM"),
  PETTY_CASH_COMMENT_EMAIL_TO_EMPLOYEE("COMMENT ON PETTY CASH"),
  FLOAT_COMMENT_EMAIL_TO_EMPLOYEE("COMMENT ON FLOAT"),
  STORES_RECEIVED_GOODS_EMAIL_TO_STAKEHOLDERS("STORE RECEIVED GOODS"),
  EMPLOYEE_ROLE_CHANGE("ROLE CHANGE"),
  EMPLOYEE_DISABLED("ACCESS DISABLED"),
  EMPLOYEE_RE_ENABLED("ACCESS ENABLED"),
  AUDITOR_FLOAT_RETIREMENT("AUDITOR FLOAT RETIREMENT"),
  GM_FLOAT_RETIREMENT("FLOAT RETIREMENT"),
  EMPLOYEE_FLOAT_DOCUMENT_UPLOAD("FLOAT DOCUMENT UPLOAD"),
  FLOAT_GM_APPROVAL("APPROVE FLOAT"),
  AUDITOR_APPROVE_PAYMENT("CHECK PAYMENT"),
  FM_APPROVE_PAYMENT("AUTHORISE PAYMENT"),
  GM_APPROVE_PAYMENT("APPROVE PAYMENT"),
  PROCUREMENT_MANAGER_ADVISE_PAYMENT("ADVISE ON PAYMENT"),
  HOD_REVIEW_QUOTATION("QUOTATION REVIEW"),
  HOD_ENDORSE_GRN("GRN ENDORSE"),
  REQUEST_ITEM_APPROVAL_GM("REQUEST ITEM APPROVAL"),
  EMPLOYEE_PASSWORD_RESET("PASSWORD RESET"),
  PAYMENT_DRAFT_COMMENT("COMMENT ON PAYMENT DRAFT"),
  QUOTATION_COMMENT_EMAIL("COMMENT ON QUOTATION"),
  STORES_MANAGER_APPROVE_GRN("STORES_MANAGER_APPROVE_GRN");

  private String emailType;

  EmailType(String emailType) {
    this.emailType = emailType;
  }
}
