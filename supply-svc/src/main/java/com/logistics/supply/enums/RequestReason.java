package com.logistics.supply.enums;

public enum RequestReason {
  Replace("REPLACE"),
  Restock("RESTOCK"),
  FreshNeed("FRESH-NEED"),
  Servicing("SERVICING"),
  Renovation("RENOVATION"),
  NewProject("NEW-PROJECT");

  private String requestReason;

  RequestReason(String requestReason) {
    this.requestReason = requestReason;
  }

  public String getRequestReason() {
    return requestReason;
  }
}
