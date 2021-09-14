package com.logistics.supply.service;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import com.logistics.supply.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentDraftService {

  @Autowired PaymentDraftRepository paymentDraftRepository;
  @Autowired PaymentRepository paymentRepository;
  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;

  public PaymentDraft savePaymentDraft(PaymentDraft draft) {
    return paymentDraftRepository.save(draft);
  }

  @Transactional(rollbackFor = Exception.class)
  public Payment approvalByAuditor(int paymentDraftId, String status, String comment) {
    Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
    if (draft.isPresent()) {
      try {
        paymentDraftRepository.approvePaymentDraft(
            comment, Boolean.parseBoolean(status), paymentDraftId);
        PaymentDraft result = findByDraftId(paymentDraftId);
        if (Boolean.parseBoolean(status)) {
          System.out.println("Convert draft to actual payment");
          Payment payment = acceptPaymentDraft(result);
          paymentDraftRepository.deleteById(result.getId());
          return payment;
        }
      } catch (Exception e) {
        log.error(e.toString());
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

      BeanUtils.copyProperties(paymentDraftDTO, d);
      grn.ifPresent(d::setGoodsReceivedNote);

      try {
        return paymentDraftRepository.save(d);
      } catch (Exception e) {
        log.error(e.toString());
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
      log.error(e.toString());
    }
    return null;
  }

  public PaymentDraft findByDraftId(int paymentDraftId) {
    try {
      Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
      return draft.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public List<PaymentDraft> findAllDrafts() {
    List<PaymentDraft> drafts = new ArrayList<>();
    try {
      drafts.addAll(paymentDraftRepository.findAll());
      return drafts;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return drafts;
  }
}
