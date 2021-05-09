package com.logistics.supply.service;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraft;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
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
      try {
        paymentDraftRepository.approvePaymentDraft(comment, status, paymentDraftId);
      } catch (Exception e) {
        e.printStackTrace();
      }
      PaymentDraft pd = findByDraftId(paymentDraftId);
      if (pd.getApprovalFromAuditor() == Boolean.TRUE) {
        try {
          System.out.println("Convert draft to actual payment");
          Payment payment = acceptPaymentDraft(draft.get());
          paymentDraftRepository.deleteById(draft.get().getId());
          return payment;

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public PaymentDraft updatePaymentDraft(int paymentDraftId, PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
    if (draft.isPresent()) {
      PaymentDraft d = draft.get();
      Optional<GoodsReceivedNote> grn =
          goodsReceivedNoteRepository.findById(paymentDraftDTO.getGoodsReceivedNote().getId());

      //      Optional<Invoice> invoice =
      // invoiceRepository.findById(paymentDraftDTO.getInvoice().getId());

      BeanUtils.copyProperties(paymentDraftDTO, d);
      grn.ifPresent(d::setGoodsReceivedNote);
      //      invoice.ifPresent(d::setInvoice);

      try {
        return paymentDraftRepository.save(d);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  private Payment acceptPaymentDraft(PaymentDraft paymentDraft) {
    System.out.println("paymentDraft = " + paymentDraft.toString());
    Payment payment = new Payment();
    payment.setPaymentAmount(paymentDraft.getPaymentAmount());
    payment.setPaymentMethod(paymentDraft.getPaymentMethod());
    payment.setBank(paymentDraft.getBank());
    payment.setGoodsReceivedNote(paymentDraft.getGoodsReceivedNote());
    payment.setChequeNumber(paymentDraft.getChequeNumber());
    payment.setPaymentStatus(paymentDraft.getPaymentStatus());
    payment.setPaymentDraftId(paymentDraft.getId());
    payment.setPurchaseNumber(paymentDraft.getPurchaseNumber());
    payment.setApprovalFromAuditor(paymentDraft.getApprovalFromAuditor());
    payment.setWithHoldingTaxAmount(paymentDraft.getWithHoldingTaxAmount());
    try {
      Payment p = paymentRepository.save(payment);
      return p;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public PaymentDraft findByDraftId(int paymentDraftId) {
    try {
      Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
      return draft.get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
