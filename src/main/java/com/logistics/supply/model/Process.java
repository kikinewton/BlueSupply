package com.logistics.supply.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum Process {
  HOD_REQUEST_ENDORSEMENT,
  PRICE_DETAILS_PROCUREMENT,
  PRICE_REVIEW_HOD,
  REQUEST_APPROVAL_GM,
  LPO_PROCUREMENT,
  GRN_STORES,
  REVIEW_GRN_HOD,
  REVIEW_GRN_PROCUREMENT,
  REVIEW_GRN_ACCOUNTS;

}
