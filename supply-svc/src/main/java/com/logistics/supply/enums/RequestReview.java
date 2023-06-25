package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum RequestReview {
  HOD_REVIEW("HOD_REVIEW"),
  GM_REVIEW("GM_REVIEW"),
  HOD_PAUSE("HOD_PAUSE"),
  COMMENT("COMMENT"),
  HOD_CANCEL("HOD_CANCEL"),
  PENDING("PENDING");


  private String requestReview;

  RequestReview(String requestReview) {
    this.requestReview = requestReview;
  }
}
