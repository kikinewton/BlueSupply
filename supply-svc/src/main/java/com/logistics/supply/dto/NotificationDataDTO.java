package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class NotificationDataDTO {
  private int requestPendingEndorsementHOD;
  private int quotationPendingReviewHOD;
  private int pettyCashPendingEndorsement;
  private int floatPendingEndorsement;
  private int requestEndorsedByHOD;
  private int requestPendingApprovalGM;
  private int pettyCashPendingApprovalGM;
  private int floatPendingApprovalGM;
  private int retireFloatPendingApprovalGM;
  private int paymentDraftPendingApproval;
  private int grnPendingApprovalGM;
  private int paymentDraftPendingAuditorCheck;
  private int retireFloatPendingAuditorCheck;
  private int floatToCloseAccount;
  private int floatToAllocateFundsAccount;
  private int pettyCashToAllocateFundsAccount;
  private int grnReadyForPaymentAccount;
  private int grnPendingApproval;
  private int assignSupplierProcurement;
  private int quotationLinkedToLpo;
  private int lpoDraftAwaitingApproval;
  private int supplierWithNoDocument;
  private int grnAwaitingPaymentAdvice;
  private int paymentDraftPendingAuthorizationFM;
  private int lpoWithoutGRN;
}
