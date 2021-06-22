package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum RequestReview {
  HOD_REVIEW("HOD_REVIEW"),
  GM_REVIEW("GM_REVIEW"),
  HOD_CANCEL("HOD_CANCEL"),
  GM_CANCEL("HOD_CANCEL");

  String requestReview;

  RequestReview(String requestReview) {
    this.requestReview = requestReview;
  }
}
