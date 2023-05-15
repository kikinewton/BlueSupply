package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.dto.QuotationMinorDTO;
import com.logistics.supply.model.QuotationComment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.interfaces.GenericConverter;

@Component
public class QuotationCommentConverter
    implements GenericConverter<QuotationComment, CommentResponse<QuotationMinorDTO>> {

  @Override
  public CommentResponse<QuotationMinorDTO> apply(QuotationComment comment) {
    CommentResponse<QuotationMinorDTO> commentResponse = new CommentResponse<>();
    BeanUtils.copyProperties(comment, commentResponse);
    EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(comment.getEmployee());
    commentResponse.setCommentBy(employeeMinorDTO);
    QuotationMinorDTO quotationMinorDTO = QuotationMinorDTO.toDto(comment.getQuotation());
    commentResponse.setItem(quotationMinorDTO);
    return commentResponse;
  }
}
