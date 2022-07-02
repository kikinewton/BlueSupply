package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.PaymentDraftMinorDTO;
import com.logistics.supply.dto.converter.PaymentDraftCommentConverter;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.model.PaymentDraftComment;
import com.logistics.supply.repository.PaymentDraftCommentRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.logistics.supply.util.Constants.PAYMENT_DRAFT_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDraftCommentService
    implements ICommentService<PaymentDraftComment, PaymentDraftMinorDTO> {
  private final PaymentDraftRepository paymentDraftRepository;
  private final PaymentDraftCommentRepository paymentDraftCommentRepository;

  private final PaymentDraftCommentConverter commentConverter;

  @Transactional(rollbackFor = Exception.class)
  private PaymentDraftComment saveComment(PaymentDraftComment draftComment) {
    return paymentDraftCommentRepository.save(draftComment);
  }

  public PaymentDraftComment addComment(PaymentDraftComment comment) {
    PaymentDraftComment saved = saveComment(comment);
    CompletableFuture.runAsync(
        () -> {
          PaymentDraft draft = saved.getPaymentDraft();
          draft.setPaymentStatus(PaymentStatus.DISPUTED);
          paymentDraftRepository.save(draft);
        });
    return saved;
  }

  @Override
  public List<CommentResponse<PaymentDraftMinorDTO>> findUnReadComment(int employeeId) {
    List<PaymentDraftComment> unReadComments =
        paymentDraftCommentRepository.findByReadFalseAndEmployeeId(employeeId);
    return commentConverter.convert(unReadComments);
  }

  @Override
  @Cacheable(value = "paymentDraftComment", key = "#id", unless = "#result == #result.isEmpty()")
  public List<CommentResponse<PaymentDraftMinorDTO>> findByCommentTypeId(int id) {
    List<PaymentDraftComment> paymentDraftComments =
        paymentDraftCommentRepository.findByPaymentDraftId(id);
    return commentConverter.convert(paymentDraftComments);
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public CommentResponse<PaymentDraftMinorDTO> savePaymentDraftComment(
      CommentDTO comment, int paymentDraftId, Employee employee) {
    PaymentDraft draft =
        paymentDraftRepository
            .findById(paymentDraftId)
            .orElseThrow(() -> new GeneralException(PAYMENT_DRAFT_NOT_FOUND, HttpStatus.NOT_FOUND));
    PaymentDraftComment draftComment =
        PaymentDraftComment.builder()
            .paymentDraft(draft)
            .description(comment.getDescription())
            .processWithComment(comment.getProcess())
            .employee(employee)
            .build();
    return commentConverter.convert(addComment(draftComment));
  }
}
