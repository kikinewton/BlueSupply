package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum RequestStatus {
  PROCESSED("PROCESSED"),
  ENDORSEMENT_CANCELLED("ENDORSEMENT_CANCELLED"),
  ENDORSED("ENDORSED"),
  APPROVED("APPROVED"),
  APPROVAL_CANCELLED("APPROVAL_CANCELLED"),
  HOD_PROCUREMENT_REVIEW("HOD_PROCUREMENT_REVIEW"),
  PENDING("PENDING");

  private String requestStatus;

  RequestStatus(String requestStatus) {
    this.requestStatus = requestStatus;
  }
}
