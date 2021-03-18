package com.logistics.supply.util;

public class Constants {
  public static final String BASE_URL = "http://localhost:8080";

  public static final String SUCCESS = "SUCCESS";
  public static final String ERROR = "ERROR";
  public static final String REQUEST_ENDORSED = "REQUEST_ENDORSED";
  public static final String REQUEST_ENDORSEMENT_DENIED = "REQUEST_ENDORSEMENT_DENIED";
  public static final String REQUEST_APPROVAL_DENIED = "REQUEST_APPROVAL_DENIED";
  public static final String REQUEST_APPROVED = "REQUEST_APPROVED";
  public static final String REQUEST_PENDING = "REQUEST_PENDING";
  public static final String REQUEST_CANCELLED = "REQUEST_CANCELLED";

  public static final String NEW_EMPLOYEE_CONFIRMATION_MAIL =
      "Please click on the link below to activate account";
  //  public static final String NEW_REQUEST_MAIL =
  //      "Please click on the link below to view a new request pending review for endorsement";
  public static final String REQUEST_ENDORSEMENT_MAIL =
      "Please click on the link below to view a new request pending review for endorsement";
  public static final String PROCUREMENT_DETAILS_MAIL =
      "Please click on the link below to view an endorsed request pending procurement information details";
  public static final String REQUEST_APPROVAL_MAIL =
      "Please click on the link below to view an endorsed request pending approval";
  public static final String REQUEST_CANCELLATION_MAIL =
      "Please click on the link below to view a cancelled request";
  public static final String NEW_USER_PASSWORD_MAIL = "Kindly find below your user credentials: \n";

  public static final String REQUEST_PENDING_APPROVAL_LINK =  BASE_URL + "/";
  public static final String REQUEST_PENDING_ENDORSEMENT_LINK =  BASE_URL + "/";
  public static final String REQUEST_PENDING_PROCUREMENT_DETAILS_LINK =  BASE_URL + "/";

  public static final String REQUEST_PENDING_APPROVAL_TITLE = "REQUEST PENDING APPROVAL";
  public static final String REQUEST_PENDING_ENDORSEMENT_TITLE =  "REQUEST PENDING ENDORSEMENT";
  public static final String REQUEST_PENDING_PROCUREMENT_DETAILS_TITLE =  "REQUEST PENDING PROCUREMENT DETAILS";

}
