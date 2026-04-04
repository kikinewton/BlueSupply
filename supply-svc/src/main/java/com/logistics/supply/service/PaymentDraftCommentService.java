package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDto;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.dto.PaymentDraftMinorDto;
import com.logistics.supply.exception.PaymentNotFoundException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraftComment;
import com.logistics.supply.repository.PaymentDraftCommentRepository;
import com.logistics.supply.repository.PaymentRepository;
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

  private final PaymentRepository paymentRepository;
  private final PaymentDraftCommentRepository paymentDraftCommentRepository;

  @Transactional(rollbackFor = Exception.class)
  private PaymentDraftComment saveComment(PaymentDraftComment draftComment) {
    return paymentDraftCommentRepository.save(draftComment);
  }

  public PaymentDraftComment addComment(PaymentDraftComment comment) {
    return saveComment(comment);
  }

  @Override
  @Cacheable(value = "paymentDraftComment", key = "#id", unless = "#result == null || #result.isEmpty()")
  public List<CommentResponse<PaymentDraftMinorDto>> findByCommentTypeId(int id) {
    List<PaymentDraftComment> comments = paymentDraftCommentRepository.findByPaymentId(id);
    return comments.stream()
        .map(c -> CommentResponse.from(c,
            EmployeeMinorDto.toDto(c.getEmployee()),
            PaymentDraftMinorDto.toDto(c.getPayment())))
        .toList();
  }

  @Override
  public ByteArrayInputStream getCommentDataSheet(int id) throws IOException {
    List<PaymentDraftComment> comments = paymentDraftCommentRepository.findByPaymentId(id);
    List<List<String>> rows =
        comments.stream()
            .map(
                p ->
                    Arrays.asList(
                        String.valueOf(p.getId()),
                        p.getPayment().getPurchaseNumber(),
                        p.getDescription(),
                        String.valueOf(p.getCreatedDate()),
                        p.getProcessWithComment().name(),
                        p.getEmployee().getFullName()))
            .collect(Collectors.toList());
    return CsvFileGenerator.toCSV(rows);
  }

  public CommentResponse<PaymentDraftMinorDto> savePaymentDraftComment(
      CommentDto comment, int paymentId, Employee employee) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

    PaymentDraftComment draftComment =
        PaymentDraftComment.builder()
            .payment(payment)
            .description(comment.getDescription())
            .processWithComment(comment.getProcess())
            .employee(employee)
            .build();
    PaymentDraftComment saved = addComment(draftComment);
    return CommentResponse.from(saved,
        EmployeeMinorDto.toDto(saved.getEmployee()),
        PaymentDraftMinorDto.toDto(saved.getPayment()));
  }
}
