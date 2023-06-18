package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.model.FloatComment;
import com.logistics.supply.model.FloatOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.interfaces.GenericConverter;

@Component
public class FloatCommentConverter
    implements GenericConverter<FloatComment, CommentResponse<FloatOrder.FloatOrderDto>> {

  @Override
  public CommentResponse<FloatOrder.FloatOrderDto> apply(FloatComment floatComment) {
    CommentResponse<FloatOrder.FloatOrderDto> commentResponse = new CommentResponse<>();
    EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(floatComment.getEmployee());
    BeanUtils.copyProperties(floatComment, commentResponse);
    FloatOrder.FloatOrderDto floatOrderDTO =
        FloatOrder.FloatOrderDto.toDto(floatComment.getFloats());
    commentResponse.setItem(floatOrderDTO);
    commentResponse.setCommentBy(employeeMinorDTO);
    return commentResponse;
  }
}
