package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum RequestReview {
  HOD_REVIEW("HOD_REVIEW"),
  HOD_PAUSE("HOD_PAUSE"),
  COMMENT("COMMENT"),
  HOD_CANCEL("HOD_CANCEL");


  String requestReview;

  RequestReview(String requestReview) {
    this.requestReview = requestReview;
  }
}
