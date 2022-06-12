package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.model.PaymentDraftComment;
import com.logistics.supply.repository.PaymentDraftCommentRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
      return paymentDraftCommentRepository.save(draftComment);
  }

  public List<PaymentDraftComment> findByPaymentDraft(PaymentDraft paymentDraft) {
      return paymentDraftCommentRepository.findByPaymentDraftOrderByIdDesc(paymentDraft);
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

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public PaymentDraftComment savePaymentDraftComment(
      CommentDTO comment, int paymentDraftId, Employee employee) {
    PaymentDraft draft =
        paymentDraftRepository
            .findById(paymentDraftId)
            .orElseThrow(
                () -> new GeneralException("Payment draft not found", HttpStatus.NOT_FOUND));
      PaymentDraftComment draftComment = new PaymentDraftComment();
      draftComment.setPaymentDraft(draft);
      draftComment.setDescription(comment.getDescription());
      draftComment.setProcessWithComment(comment.getProcess());
      draftComment.setEmployee(employee);
      return addComment(draftComment);
  }
}
