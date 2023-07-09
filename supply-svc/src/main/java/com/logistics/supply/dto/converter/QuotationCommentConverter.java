package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.dto.QuotationMinorDto;
import com.logistics.supply.model.QuotationComment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.interfaces.GenericConverter;

@Component
public class QuotationCommentConverter
    implements GenericConverter<QuotationComment, CommentResponse<QuotationMinorDto>> {

  @Override
  public CommentResponse<QuotationMinorDto> apply(QuotationComment comment) {
    CommentResponse<QuotationMinorDto> commentResponse = new CommentResponse<>();
    BeanUtils.copyProperties(comment, commentResponse);
    EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(comment.getEmployee());
    commentResponse.setCommentBy(employeeMinorDTO);
    QuotationMinorDto quotationMinorDTO = QuotationMinorDto.toDto(comment.getQuotation());
    commentResponse.setItem(quotationMinorDTO);
    return commentResponse;
  }
}
