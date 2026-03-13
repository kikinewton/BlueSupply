package com.logistics.supply.service;

import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.exception.RequestItemNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.*;
import com.logistics.supply.repository.RequestItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackRequestStatusService {
  private final RequestItemRepository requestItemRepository;
  private final LocalPurchaseOrderRepository localPurchaseOrderRepository;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  private final PaymentDraftRepository paymentDraftRepository;
  private final PaymentRepository paymentRepository;

  @Cacheable(value = "requestStage", key = "{ #requestItemId }", unless = "#result == null")
  public TrackRequestDTO getRequestStage(int requestItemId) {
    RequestItem requestItem = requestItemRepository.findById(requestItemId)
            .orElseThrow(() -> new RequestItemNotFoundException(requestItemId));

    TrackRequestDTO trackRequest = TrackRequestDTO.fromRequestItem(requestItem);
    if (!RequestApproval.APPROVED.equals(requestItem.getApproval())
        || !localPurchaseOrderRepository.lpoExistByRequestItem(requestItemId)) {
        return trackRequest;
    }
    trackRequest.setLpoIssued("LPO ISSUED");
    LocalPurchaseOrder lpo =
        localPurchaseOrderRepository
            .findLpoByRequestItem(requestItemId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "LPO related to request item id %s not found".formatted(requestItemId)));
    Optional<GoodsReceivedNote> grn = goodsReceivedNoteRepository.findByLocalPurchaseOrder(lpo);
    if (grn.isEmpty()) return trackRequest;
    trackRequest.setGrnIssued("GRN ISSUED");
    if (grn.get().isApprovedByHod()) trackRequest.setGrnHodEndorse("GRN HOD ENDORSED");
    if (grn.get().getPaymentDate() != null)
      trackRequest.setProcurementAdvise("PROCUREMENT PAYMENT ADVICE");

    if (paymentRepository.existsByGoodsReceivedNote(grn.get())) {
      trackRequest.setPaymentInitiated("ACCOUNT INITIATED PAYMENT");
      trackRequest.setPaymentAuditorCheck("AUDITOR PAYMENT CHECK");
      trackRequest.setPaymentFMAuthorise("FM PAYMENT AUTHORIZATION");
      trackRequest.setPaymentGMApprove("GM PAYMENT APPROVAL");
      return trackRequest;
    }

    if (paymentDraftRepository.existsByGoodsReceivedNote(grn.get())) {
      PaymentDraft paymentDraft = paymentDraftRepository.findByGoodsReceivedNote(grn.get()).get();
      trackRequest.setPaymentInitiated("ACCOUNT INITIATED PAYMENT");
      if (paymentDraft.getApprovalFromAuditor() != null && paymentDraft.getApprovalFromAuditor())
        trackRequest.setPaymentAuditorCheck("AUDITOR PAYMENT CHECK");
      if (paymentDraft.getApprovalFromFM() != null && paymentDraft.getApprovalFromFM())
        trackRequest.setPaymentFMAuthorise("FM PAYMENT AUTHORIZATION");
      if (paymentDraft.getApprovalFromGM() != null && paymentDraft.getApprovalFromGM())
        trackRequest.setPaymentGMApprove("GM PAYMENT APPROVAL");
    }
    return trackRequest;
  }
}
