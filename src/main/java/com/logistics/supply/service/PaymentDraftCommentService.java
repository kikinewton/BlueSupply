package com.logistics.supply.service;

import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.model.PaymentDraftComment;
import com.logistics.supply.repository.PaymentDraftCommentRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDraftCommentService {

  private final PaymentDraftRepository paymentDraftRepository;
  private final PaymentDraftCommentRepository paymentDraftCommentRepository;

  @Transactional(rollbackFor = Exception.class)
  private PaymentDraftComment saveComment(PaymentDraftComment draftComment) {
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

  public PaymentDraftComment addComment(PaymentDraftComment comment) {
    try {
      PaymentDraftComment saved = saveComment(comment);
      if (Objects.nonNull(saved)) {
        return paymentDraftRepository
            .findById(saved.getPaymentDraft().getId())
            .map(
                p -> {
                  p.setPaymentStatus(PaymentStatus.DISPUTED);
                  PaymentDraft d = paymentDraftRepository.save(p);
                  if (Objects.nonNull(d)) return saved;
                  return null;
                })
            .orElse(null);
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }
}
