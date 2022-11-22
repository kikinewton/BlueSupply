package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.dto.FloatGrnDTO;
import com.logistics.supply.interfaces.GenericConverter;
import com.logistics.supply.model.FloatGrnComment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class FloatGRNCommentConverter
    implements GenericConverter<FloatGrnComment, CommentResponse<FloatGrnDTO>> {

  @Override
  public CommentResponse<FloatGrnDTO> apply(FloatGrnComment floatGRNComment) {
    CommentResponse<FloatGrnDTO> commentResponse = new CommentResponse<>();
    EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(floatGRNComment.getEmployee());
    BeanUtils.copyProperties(floatGRNComment, commentResponse);
    FloatGrnDTO floatGrnDTO = FloatGrnDTO.toDto(floatGRNComment.getFloatGRN());
    commentResponse.setCommentBy(employeeMinorDTO);
    commentResponse.setItem(floatGrnDTO);
    return commentResponse;
  }
}
