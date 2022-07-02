package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.QuotationMinorDTO;
import com.logistics.supply.dto.converter.QuotationCommentConverter;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.QuotationComment;
import com.logistics.supply.repository.QuotationCommentRepository;
import com.logistics.supply.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static com.logistics.supply.util.Constants.QUOTATION_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class QuotationCommentService implements ICommentService<QuotationComment, QuotationMinorDTO> {
  private final QuotationRepository quotationRepository;
  private final QuotationCommentRepository quotationCommentRepository;
  private final QuotationCommentConverter commentConverter;

  public CommentResponse<QuotationMinorDTO> saveComment(CommentDTO comment, int quotationId, Employee employee) throws GeneralException {
    Quotation quotation =
        quotationRepository
            .findById(quotationId)
            .orElseThrow(() -> new GeneralException(QUOTATION_NOT_FOUND, HttpStatus.NOT_FOUND));
    QuotationComment quotationComment =
        QuotationComment.builder()
            .quotation(quotation)
            .description(comment.getDescription())
            .processWithComment(comment.getProcess())
            .employee(employee)
            .build();
    return commentConverter.convert(addComment(quotationComment));
  }

  @Override
  public QuotationComment addComment(QuotationComment comment) {

    return quotationCommentRepository.save(comment);
  }

  @Override
  public List<CommentResponse<QuotationMinorDTO>> findUnReadComment(int employeeId) {
    return findUnReadQuotationComment(employeeId);
  }


  private List<CommentResponse<QuotationMinorDTO>> findUnReadQuotationComment(int employeeId) {
    List<QuotationComment> unReadComment = quotationCommentRepository.findByReadFalseAndEmployeeId(employeeId);
    List<CommentResponse<QuotationMinorDTO>> commentResponses = commentConverter.convert(unReadComment);
    return commentResponses;
  }

  @Override
  public List<CommentResponse<QuotationMinorDTO>> findByCommentTypeId(int id) {
    List<QuotationComment> unReadComment = quotationCommentRepository.findByQuotationId(id);
    List<CommentResponse<QuotationMinorDTO>> commentResponse = commentConverter.convert(unReadComment);
    return commentResponse;
  }
}
