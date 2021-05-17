package com.logistics.supply.util;

import org.springframework.beans.factory.annotation.Value;

public class Constants {

  public static final String tableHeader = "<th>%s</th>";
  public static final String tableData = "<td>%s</td>";
  public static final String tableRow = "<tr>%s</tr>";

  public static final String BASE_URL =
      "https://etornamtechnologies.github.io/skyblue-request-frontend-react/#";

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
  public static final String EMPLOYEE_REQUEST_ENDORSED_MAIL =
      "Kindly note that your request has been endorsed by your HOD";
  public static final String REQUEST_QUOTATION_FROM_PROCUREMENT_MAIL =
      "Kindly click on the link below to view quotations from suppliers %s :\n";
  public static final String REQUEST_ENDORSEMENT_MAIL =
      "Please click on the link below to view new request pending review for endorsement";
  public static final String PROCUREMENT_DETAILS_MAIL =
      "Please click on the link below to view endorsed request(s) pending procurement information details";
  public static final String REQUEST_APPROVAL_MAIL =
      "Please click on the link below to view an endorsed request pending approval";
  public static final String APPROVED_REQUEST_MAIL =
      "Please click on the link below to view an approved request";
  public static final String NEW_USER_PASSWORD_MAIL = "Kindly find below your user credentials: \n";

  public static final String REQUEST_PENDING_APPROVAL_LINK =
      BASE_URL + "/request-management/general-manager-item-requests";
  public static final String APPROVED_REQUEST_LINK = BASE_URL + "/";
  public static final String REQUEST_PENDING_ENDORSEMENT_LINK =
      BASE_URL + "/request-management/hod-item-requests";
  public static final String REQUEST_PENDING_PROCUREMENT_DETAILS_LINK =
      BASE_URL + "/request-management/my-requests";

  public static final String LPO_ADDED_NOTIFICATION =
      "Please click on the link below to view newly added LPO from Procurement";

  public static final String LPO_LINK = BASE_URL + "/request-management";

  public static final String REQUEST_QUOTATION_FROM_PROCUREMENT_LINK = BASE_URL + "";

  public static final String REQUEST_PENDING_APPROVAL_TITLE = "REQUEST PENDING APPROVAL";
  public static final String REQUEST_PENDING_ENDORSEMENT_TITLE = "REQUEST PENDING ENDORSEMENT";
  public static final String REQUEST_APPROVED_TITLE = "APPROVED REQUEST";
  public static final String REQUEST_PENDING_PROCUREMENT_DETAILS_TITLE =
      "REQUEST PENDING PROCUREMENT DETAILS";

  public static final String REQUEST_APPROVAL_MAIL_TO_EMPLOYEE =
      "Kindly note that your request item(s) listed below have been approved";

  public static final String REQUEST_CANCELLED_MAIL_TO_EMPLOYEE =
          "Kindly note that your request item(s) listed below have been cancelled";


  public static final String DEFAULT_EMAIL = "bsupply901@gmail.com";
  public static final String DEFAULT_PROCUREMENT_MAIL = "bsupply901@gmail.com";

  public static final String DEFAULT_STORES_EMAIL = "bsupply901@gmail.com";

  public static String EMAIL_URI = "http://localhost:1234/api/sendEmail";

  @Value("${email.uri}")
  public void setEmailUri(String emailUri) {
    EMAIL_URI = emailUri;
  }

  public static final String[] procured_items_header = {
    "id",
    "name",
    "reason",
    "purpose",
    "quantity",
    "total_price",
    "user_department",
    "request_category",
    "supplied_by"
  };

  public static final String[] payment_report_header = {
    "id",
    "supplier",
    "invoice_no",
    "account_number",
    "cheque_number",
    "purchase_number",
    "payment_due_date",
    "paid_amount",
    "payment_status",
    "created_date",
    "wht_amount"
  };

  public static final String[] grn_report_header = {
    "id", "request_item", "supplier", "invoice_number", "date_received"
  };

  public static final String payment_due_reminder_message =
      "Please note that the payment for suppliers listed below will be due in 7 days or less";
}
