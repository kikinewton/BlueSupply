package com.logistics.supply.service;

import com.logistics.supply.enums.PaymentStage;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStageLabel;
import com.logistics.supply.exception.RequestItemNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.*;
import com.logistics.supply.repository.RequestItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackRequestStatusService {
  private final RequestItemRepository requestItemRepository;
  private final LocalPurchaseOrderRepository localPurchaseOrderRepository;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  private final PaymentRepository paymentRepository;

  public TrackRequestDto getRequestStage(int requestItemId) {
    RequestItem requestItem = requestItemRepository.findById(requestItemId)
            .orElseThrow(() -> new RequestItemNotFoundException(requestItemId));

    TrackRequestDto trackRequest = TrackRequestDto.fromRequestItem(requestItem);
    if (!RequestApproval.APPROVED.equals(requestItem.getApproval())) {
        return trackRequest;
    }
    Optional<LocalPurchaseOrder> lpoOpt = localPurchaseOrderRepository.findLpoByRequestItem(requestItemId);
    if (lpoOpt.isEmpty()) return trackRequest;
    LocalPurchaseOrder lpo = lpoOpt.get();
    trackRequest.setLpoIssued(RequestStageLabel.LPO_ISSUED.getLabel());
    trackRequest.setLpoIssuedDate(lpo.getCreatedAt());

    Optional<GoodsReceivedNote> grn = goodsReceivedNoteRepository.findByLocalPurchaseOrder(lpo);
    if (grn.isEmpty()) return trackRequest;
    GoodsReceivedNote grnEntity = grn.get();
    trackRequest.setGrnIssued(RequestStageLabel.GRN_ISSUED.getLabel());
    trackRequest.setGrnIssuedDate(Date.from(grnEntity.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant()));

    if (grnEntity.isApprovedByHod()) {
      trackRequest.setGrnHodEndorse(RequestStageLabel.GRN_HOD_ENDORSED.getLabel());
      trackRequest.setGrnHodEndorseDate(grnEntity.getDateOfApprovalByHod());
    }
    if (grnEntity.getPaymentDate() != null) {
      trackRequest.setProcurementAdvise(RequestStageLabel.PROCUREMENT_PAYMENT_ADVICE.getLabel());
      trackRequest.setProcurementAdviseDate(grnEntity.getPaymentDate());
    }

    // Check for a fully approved payment against this GRN
    if (paymentRepository.existsByGoodsReceivedNoteAndStage(grnEntity, PaymentStage.FULLY_APPROVED)) {
      Optional<Payment> fullyApproved =
          paymentRepository.findByGoodsReceivedNoteAndStageNot(grnEntity, PaymentStage.FULLY_APPROVED);
      fullyApproved.ifPresent(p -> {
        trackRequest.setPaymentInitiated(RequestStageLabel.ACCOUNT_INITIATED_PAYMENT.getLabel());
        LocalDateTime createdDate = p.getCreatedDate().get();
        trackRequest.setPaymentInitiatedDate(Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant()));
        trackRequest.setPaymentAuditorCheck(RequestStageLabel.AUDITOR_PAYMENT_CHECK.getLabel());
        trackRequest.setPaymentAuditorCheckDate(p.getApprovalByAuditorDate());
        trackRequest.setPaymentFMAuthorise(RequestStageLabel.FM_PAYMENT_AUTHORIZATION.getLabel());
        trackRequest.setPaymentFMAuthoriseDate(p.getApprovalByFMDate());
        trackRequest.setPaymentGMApprove(RequestStageLabel.GM_PAYMENT_APPROVAL.getLabel());
        trackRequest.setPaymentGMApproveDate(p.getApprovalByGMDate());
      });
      return trackRequest;
    }

    // Check for an in-progress payment (DRAFT, AUDITOR_APPROVED, or FM_APPROVED)
    Optional<Payment> inProgressPayment =
        paymentRepository.findByGoodsReceivedNoteAndStageNot(grnEntity, PaymentStage.FULLY_APPROVED);
    if (inProgressPayment.isPresent()) {
      Payment payment = inProgressPayment.get();
      trackRequest.setPaymentInitiated(RequestStageLabel.ACCOUNT_INITIATED_PAYMENT.getLabel());
      LocalDateTime localDateTime = payment.getCreatedDate().get();
      trackRequest.setPaymentInitiatedDate(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
      if (Boolean.TRUE.equals(payment.getApprovalFromAuditor())) {
        trackRequest.setPaymentAuditorCheck(RequestStageLabel.AUDITOR_PAYMENT_CHECK.getLabel());
        trackRequest.setPaymentAuditorCheckDate(payment.getApprovalByAuditorDate());
      }
      if (Boolean.TRUE.equals(payment.getApprovalFromFM())) {
        trackRequest.setPaymentFMAuthorise(RequestStageLabel.FM_PAYMENT_AUTHORIZATION.getLabel());
        trackRequest.setPaymentFMAuthoriseDate(payment.getApprovalByFMDate());
      }
      if (Boolean.TRUE.equals(payment.getApprovalFromGM())) {
        trackRequest.setPaymentGMApprove(RequestStageLabel.GM_PAYMENT_APPROVAL.getLabel());
        trackRequest.setPaymentGMApproveDate(payment.getApprovalByGMDate());
      }
    }
    return trackRequest;
  }
}
