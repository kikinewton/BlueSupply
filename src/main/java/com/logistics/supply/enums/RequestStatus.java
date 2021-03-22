package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum RequestStatus {
  PROCESSED("PROCESSED"),
  ENDORSEMENT_CANCELLED("ENDORSEMENT_CANCELLED"),
  APPROVAL_CANCELLED("APPROVAL_CANCELLED"),
  PENDING("PENDING");

  private String requestStatus;

  RequestStatus(String requestStatus) {
    this.requestStatus = requestStatus;
  }
}
