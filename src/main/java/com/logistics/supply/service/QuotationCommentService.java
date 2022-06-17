package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.QuotationComment;
import com.logistics.supply.repository.QuotationCommentRepository;
import com.logistics.supply.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class QuotationCommentService implements ICommentService<QuotationComment> {
  private final QuotationRepository quotationRepository;
  private final QuotationCommentRepository quotationCommentRepository;

  @SneakyThrows
  private QuotationComment saveComment(CommentDTO comment, int quotationId, Employee employee) {
    Quotation quotation =
        quotationRepository
            .findById(quotationId)
            .orElseThrow(() -> new GeneralException("", HttpStatus.NOT_FOUND));
    QuotationComment quotationComment =
        QuotationComment.builder()
            .quotation(quotation)
            .description(comment.getDescription())
            .processWithComment(comment.getProcess())
            .employee(employee)
            .build();
    return addComment(quotationComment);
  }

  @Override
  public QuotationComment addComment(QuotationComment comment) {
    return quotationCommentRepository.save(comment);
  }

  @Override
  public List<QuotationComment> findUnReadComment(int employeeId) {
    return quotationCommentRepository.findByReadFalseAndEmployeeId(employeeId);
  }

  @Override
  public List<QuotationComment> findByCommentTypeId(int id) {
    return quotationCommentRepository.findByQuotationId(id);
  }
}
