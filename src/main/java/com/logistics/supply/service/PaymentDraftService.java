package com.logistics.supply.service;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraft;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class PaymentDraftService extends AbstractDataService {

  public PaymentDraft savePaymentDraft(PaymentDraft draft) {
    return paymentDraftRepository.save(draft);
  }

  @Transactional(rollbackFor = Exception.class)
  public Payment approvalByAuditor(int paymentDraftId, boolean status, String comment) {
    Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
    if (draft.isPresent()) {
      draft.get().setApprovalFromAuditor(status);
      draft.get().setAuditorComment(comment);
      draft.get().setApprovalByAuditorDate(new Date());
      if (draft.get().getApprovalFromAuditor() == Boolean.TRUE) {
        try {
          var payment = acceptPaymentDraft(draft.get());
          paymentDraftRepository.deleteById(draft.get().getId());
          return payment;

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public PaymentDraft updatePaymentDraft(int paymentDraftId, PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
    if (draft.isPresent()) {
      var d = draft.get();
      var grn =
          goodsReceivedNoteRepository
              .findById(paymentDraftDTO.getGoodsReceivedNote().getId())
              .orElseThrow(Exception::new);
      var invoice =
          invoiceRepository
              .findById(paymentDraftDTO.getInvoice().getId())
              .orElseThrow(Exception::new);
      BeanUtils.copyProperties(paymentDraftDTO, d);
      d.setInvoice(invoice);
      d.setGoodsReceivedNote(grn);
      try {
        return paymentDraftRepository.save(d);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public Payment acceptPaymentDraft(PaymentDraft paymentDraft) {
    Payment payment = new Payment();
    BeanUtils.copyProperties(paymentDraft, payment);
    payment.setPaymentDraftId(paymentDraft.getId());
    try {
      var p = paymentRepository.save(payment);
      return p;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
