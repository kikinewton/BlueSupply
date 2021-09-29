package com.logistics.supply.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum RequestType {
  SERVICE_REQUEST("SERVICE_REQUEST"),
  GOODS_REQUEST("GOODS_REQUEST"),
  PROJECT_AND_WORKS("PROJECT_AND_WORKS");

  private String requestType;
}
