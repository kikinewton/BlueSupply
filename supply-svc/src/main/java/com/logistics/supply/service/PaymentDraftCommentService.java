package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDto;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.PaymentDraftMinorDto;
import com.logistics.supply.dto.converter.PaymentDraftCommentConverter;
import com.logistics.supply.exception.PaymentDraftNotFoundException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.model.PaymentDraftComment;
import com.logistics.supply.repository.PaymentDraftCommentRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import com.logistics.supply.util.CsvFileGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDraftCommentService
    implements ICommentService<PaymentDraftComment, PaymentDraftMinorDto> {

  private final PaymentDraftRepository paymentDraftRepository;
  private final PaymentDraftCommentRepository paymentDraftCommentRepository;

  private final PaymentDraftCommentConverter commentConverter;

  @Transactional(rollbackFor = Exception.class)
  private PaymentDraftComment saveComment(PaymentDraftComment draftComment) {
    return paymentDraftCommentRepository.save(draftComment);
  }

  public PaymentDraftComment addComment(PaymentDraftComment comment) {
    return saveComment(comment);
  }

  @Override
  @Cacheable(value = "paymentDraftComment", key = "#id", unless = "#result == #result.isEmpty()")
  public List<CommentResponse<PaymentDraftMinorDto>> findByCommentTypeId(int id) {
    List<PaymentDraftComment> paymentDraftComments =
        paymentDraftCommentRepository.findByPaymentDraftId(id);
    return commentConverter.convert(paymentDraftComments);
  }

  @Override
  public ByteArrayInputStream getCommentDataSheet(int id) throws IOException {
    List<PaymentDraftComment> paymentDraftComments =
        paymentDraftCommentRepository.findByPaymentDraftId(id);
    List<List<String>> pcList =
        paymentDraftComments.stream()
            .map(
                p ->
                    Arrays.asList(
                        String.valueOf(p.getId()),
                        p.getPaymentDraft().getPurchaseNumber(),
                        p.getDescription(),
                        String.valueOf(p.getCreatedDate()),
                        p.getProcessWithComment().name(),
                        p.getEmployee().getFullName()))
            .collect(Collectors.toList());
    return CsvFileGenerator.toCSV(pcList);
  }


  public CommentResponse<PaymentDraftMinorDto> savePaymentDraftComment(
          CommentDto comment, int paymentDraftId, Employee employee) {
    PaymentDraft draft =
        paymentDraftRepository
            .findById(paymentDraftId)
            .orElseThrow(() -> new PaymentDraftNotFoundException(paymentDraftId));

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
