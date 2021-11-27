package com.logistics.supply.service;

import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.model.PaymentDraftComment;
import com.logistics.supply.repository.PaymentDraftCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDraftCommentService {
  final PaymentDraftCommentRepository paymentDraftCommentRepository;

  @Transactional(rollbackFor = Exception.class)
  public PaymentDraftComment saveComment(PaymentDraftComment draftComment) {
    try {
      return paymentDraftCommentRepository.save(draftComment);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public List<PaymentDraftComment> findByPaymentDraft(PaymentDraft paymentDraft) {
    try {
      return paymentDraftCommentRepository.findByPaymentDraftOrderByIdDesc(paymentDraft);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }
}
