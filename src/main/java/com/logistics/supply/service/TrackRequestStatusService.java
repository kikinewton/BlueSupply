package com.logistics.supply.service;

import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackRequestStatusService {
  private static LocalPurchaseOrder lpo = new LocalPurchaseOrder();
  private static PaymentDraft paymentDraft = new PaymentDraft();
  private static Optional<GoodsReceivedNote> grn = Optional.empty();

  private final RequestItemRepository requestItemRepository;
  private final LocalPurchaseOrderRepository localPurchaseOrderRepository;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  private final PaymentDraftRepository paymentDraftRepository;
  private final PaymentRepository paymentRepository;

  public TrackRequestDTO getRequestStage(int requestItemId) {
    Optional<RequestItem> ri = requestItemRepository.findById(requestItemId);
    if (!ri.isPresent()) return null;
    RequestItem item = ri.get();
    TrackRequestDTO trackRequest = new TrackRequestDTO();
    BeanUtils.copyProperties(item, trackRequest);
    if (!RequestApproval.APPROVED.equals(item.getApproval())
        || !localPurchaseOrderRepository.lpoExistByRequestItem(requestItemId)) return trackRequest;
    trackRequest.setLpoIssued("LPO ISSUED");
    if (trackRequest.getLpoIssued() != null) {
      lpo = localPurchaseOrderRepository.findLpoByRequestItem(requestItemId);
      grn = goodsReceivedNoteRepository.findByLocalPurchaseOrder(lpo);
      if (!grn.isPresent()) return trackRequest;
      trackRequest.setGrnIssued("GRN ISSUED");
      if (grn.get().isApprovedByHod()) trackRequest.setGrnHodEndorse("GRN HOD ENDORSED");
      if (grn.get().isApprovedByGm()) trackRequest.setGrnGmApprove("GRN GM APPROVED");
      if (grn.get().getPaymentDate() != null)
        trackRequest.setProcurementAdvise("PROCUREMENT PAYMENT ADVICE");
    }
    if (paymentRepository.existsByGoodsReceivedNote(grn.get())) {
      trackRequest.setPaymentInitiated("ACCOUNT INITIATED PAYMENT");
      trackRequest.setPaymentAuditorCheck("AUDITOR PAYMENT CHECK");
      trackRequest.setPaymentFMAuthorise("FM PAYMENT AUTHORIZATION");
      trackRequest.setPaymentGMApprove("GM PAYMENT APPROVAL");
      return trackRequest;
    }

    if (paymentDraftRepository.existsByGoodsReceivedNote(grn.get())) {
      paymentDraft = paymentDraftRepository.findByGoodsReceivedNote(grn.get()).get();
      trackRequest.setPaymentInitiated("ACCOUNT INITIATED PAYMENT");
      if (paymentDraft.getApprovalFromAuditor())
        trackRequest.setPaymentAuditorCheck("AUDITOR PAYMENT CHECK");
      if (paymentDraft.getApprovalFromFM())
        trackRequest.setPaymentFMAuthorise("FM PAYMENT AUTHORIZATION");
      if (paymentDraft.getApprovalFromGM()) trackRequest.setPaymentGMApprove("GM PAYMENT APPROVAL");
    }
    return trackRequest;
  }
}
