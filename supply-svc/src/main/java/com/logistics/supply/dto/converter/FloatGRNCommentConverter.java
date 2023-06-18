package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.dto.FloatGrnDto;
import com.logistics.supply.model.FloatGrnComment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.interfaces.GenericConverter;

@Component
public class FloatGRNCommentConverter
    implements GenericConverter<FloatGrnComment, CommentResponse<FloatGrnDto>> {

  @Override
  public CommentResponse<FloatGrnDto> apply(FloatGrnComment floatGRNComment) {
    CommentResponse<FloatGrnDto> commentResponse = new CommentResponse<>();
    EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(floatGRNComment.getEmployee());
    BeanUtils.copyProperties(floatGRNComment, commentResponse);
    FloatGrnDto floatGrnDTO = FloatGrnDto.toDto(floatGRNComment.getFloatGRN());
    commentResponse.setCommentBy(employeeMinorDTO);
    commentResponse.setItem(floatGrnDTO);
    return commentResponse;
  }
}
