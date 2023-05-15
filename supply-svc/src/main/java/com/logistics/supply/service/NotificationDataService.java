package com.logistics.supply.service;

import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Department;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.logistics.supply.dto.NotificationDataDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.model.EmployeeRole;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDataService {
  private final LocalPurchaseOrderDraftService localPurchaseOrderDraftService;
  private final LocalPurchaseOrderService localPurchaseOrderService;
  private final GoodsReceivedNoteService goodsReceivedNoteService;
  private final RequestItemService requestItemService;
  private final PaymentDraftService paymentDraftService;
  private final FloatOrderService floatOrderService;
  private final PettyCashService pettyCashService;
  private final QuotationService quotationService;
  private final SupplierService supplierService;
  private final EmployeeService employeeService;
  private final RoleService roleService;

  public NotificationDataDTO getNotificationData(Authentication authentication, Pageable pageable) {
    NotificationDataDTO data = new NotificationDataDTO();
    try {
      EmployeeRole employeeRole = roleService.getEmployeeRole(authentication);
      switch (employeeRole) {
        case ROLE_GENERAL_MANAGER:
          return getNotificationDataGM(data, employeeRole);
        case ROLE_HOD:
          return getNotificationDataHOD(authentication, pageable, data);
        case ROLE_AUDITOR:
          return getNotificationDataAuditor(data, employeeRole);
        case ROLE_STORE_OFFICER:
          int lpoWithoutGRN = localPurchaseOrderService.countLpoWithoutGRN();
          data.setLpoWithoutGRN(lpoWithoutGRN);
          return data;
        case ROLE_PROCUREMENT_OFFICER:
        case ROLE_PROCUREMENT_MANAGER:
          return getNotificationDataProcurement(data);
        case ROLE_FINANCIAL_MANAGER:
          return getNotificationDataFM(data, employeeRole);
        case ROLE_ACCOUNT_OFFICER:
          return getNotificationDataAccount(data);
        default:
          return null;
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  private NotificationDataDTO getNotificationDataFM(
      NotificationDataDTO data, EmployeeRole employeeRole) {
    int draftPendingAuthorizationFM =
        paymentDraftService.findAllDrafts(0, Integer.MAX_VALUE, employeeRole).size();
    data.setPaymentDraftPendingAuthorizationFM(draftPendingAuthorizationFM);
    return data;
  }

  private NotificationDataDTO getNotificationDataProcurement(NotificationDataDTO data) {
    int requestEndorsedByHOD = requestItemService.getEndorsedItemsWithoutSuppliers().size();
    int quotationLinkedToLpo = quotationService.findQuotationLinkedToLPO().size();
    int supplierWithNoDocument = supplierService.findSupplierWithNoDocFromSRM().size();
    int lpoDraftAwaitingApproval =
        localPurchaseOrderDraftService.findDraftAwaitingApproval().size();
    int grnAwaitingPaymentAdvice = goodsReceivedNoteService.findGRNRequiringPaymentDate().size();

    data.setGrnAwaitingPaymentAdvice(grnAwaitingPaymentAdvice);
    data.setSupplierWithNoDocument(supplierWithNoDocument);
    data.setLpoDraftAwaitingApproval(lpoDraftAwaitingApproval);
    data.setAssignSupplierProcurement(requestEndorsedByHOD);
    data.setQuotationLinkedToLpo(quotationLinkedToLpo);
    return data;
  }

  private NotificationDataDTO getNotificationDataAuditor(
      NotificationDataDTO data, EmployeeRole employeeRole) throws GeneralException {
    int paymentDraftPendingAuditorCheck =
        paymentDraftService.findAllDrafts(0, Integer.MAX_VALUE, employeeRole).size();
    int retireFloatPendingAuditorCheck =
        floatOrderService.floatOrderForAuditorRetire(0, Integer.MAX_VALUE).getNumberOfElements();

    data.setRetireFloatPendingAuditorCheck(retireFloatPendingAuditorCheck);
    data.setPaymentDraftPendingAuditorCheck(paymentDraftPendingAuditorCheck);
    return data;
  }

  private NotificationDataDTO getNotificationDataAccount(NotificationDataDTO data) throws GeneralException {
    int floatToCloseAccount =
        floatOrderService.findFloatOrderToClose(0, Integer.MAX_VALUE).getNumberOfElements();
    int floatToAllocateFundsAccount =
        floatOrderService
            .findByApprovalStatus(0, Integer.MAX_VALUE, RequestApproval.APPROVED)
            .getNumberOfElements();
    int pettyCashToAllocateFundsAccount = pettyCashService.findPettyCashPendingPayment().size();
    int grnReadyForPaymentAccount = goodsReceivedNoteService.findGRNWithoutCompletePayment().size();

    data.setFloatToCloseAccount(floatToCloseAccount);
    data.setFloatToAllocateFundsAccount(floatToAllocateFundsAccount);
    data.setGrnReadyForPaymentAccount(grnReadyForPaymentAccount);
    data.setPettyCashToAllocateFundsAccount(pettyCashToAllocateFundsAccount);
    return data;
  }

  private NotificationDataDTO getNotificationDataHOD(
      Authentication authentication, Pageable pageable, NotificationDataDTO data) throws GeneralException {
    Department dept = employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
    int pendingReviewHOD =
        requestItemService.findRequestItemsToBeReviewed(RequestReview.PENDING, dept.getId()).size();
    int pendingEndorsementHOD = requestItemService.getRequestItemForHOD(dept.getId()).size();
    int grnPendingApproval =
        goodsReceivedNoteService.findGRNWithoutHodApprovalPerDepartment(dept).size();
    int pettyCashPendingEndorsement = pettyCashService.findByDepartment(dept).size();
    int floatPendingEndorsement =
        floatOrderService.findPendingByDepartment(dept, pageable).getNumberOfElements();

    data.setGrnPendingApproval(grnPendingApproval);
    data.setFloatPendingEndorsement(floatPendingEndorsement);
    data.setRequestPendingEndorsementHOD(pendingEndorsementHOD);
    data.setPettyCashPendingEndorsement(pettyCashPendingEndorsement);
    data.setQuotationPendingReviewHOD(pendingReviewHOD);
    return data;
  }

  private NotificationDataDTO getNotificationDataGM(
      NotificationDataDTO data, EmployeeRole employeeRole) throws GeneralException {
    int pettyCashPendingApprovalGM = pettyCashService.findEndorsedPettyCash().size();
    int requestPendingApprovalGM =
        requestItemService.getEndorsedItemsWithAssignedSuppliers().size();
    int grnPendingApprovalGM =
        goodsReceivedNoteService.findNonApprovedGRN(RequestReview.GM_REVIEW).size();
    int paymentDraftPendingApproval =
        paymentDraftService.findAllDrafts(0, Integer.MAX_VALUE, employeeRole).size();
    int retireFloatPendingApprovalGM =
        floatOrderService.floatOrdersForGmRetire(0, Integer.MAX_VALUE).getNumberOfElements();
    int floatPendingApprovalGM =
        floatOrderService
            .findFloatsByEndorseStatus(0, Integer.MAX_VALUE, EndorsementStatus.ENDORSED)
            .getNumberOfElements();

    data.setGrnPendingApprovalGM(grnPendingApprovalGM);
    data.setPaymentDraftPendingApproval(paymentDraftPendingApproval);
    data.setFloatPendingApprovalGM(floatPendingApprovalGM);
    data.setRequestPendingApprovalGM(requestPendingApprovalGM);
    data.setRetireFloatPendingApprovalGM(retireFloatPendingApprovalGM);
    data.setPettyCashPendingApprovalGM(pettyCashPendingApprovalGM);
    return data;
  }
}
