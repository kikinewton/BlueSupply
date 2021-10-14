package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum UpdateStatus {
  HOD_REVIEW("HOD_REVIEW"),
  APPROVE("APPROVE"),
  ENDORSE("ENDORSE"),
  CANCEL("CANCEL");

  private String updateStatus;

  UpdateStatus(String updateStatus) {
    this.updateStatus = updateStatus;
  }
}
