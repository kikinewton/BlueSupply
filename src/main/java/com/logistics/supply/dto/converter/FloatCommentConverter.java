package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.interfaces.GenericConverter;
import com.logistics.supply.model.FloatComment;
import com.logistics.supply.model.FloatOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class FloatCommentConverter
    implements GenericConverter<FloatComment, CommentResponse<FloatOrder.FloatOrderDTO>> {

  @Override
  public CommentResponse<FloatOrder.FloatOrderDTO> apply(FloatComment floatComment) {
    CommentResponse<FloatOrder.FloatOrderDTO> commentResponse = new CommentResponse<>();
    EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(floatComment.getEmployee());
    BeanUtils.copyProperties(floatComment, commentResponse);
    FloatOrder.FloatOrderDTO floatOrderDTO =
        FloatOrder.FloatOrderDTO.toDto(floatComment.getFloats());
    commentResponse.setItem(floatOrderDTO);
    commentResponse.setCommentBy(employeeMinorDTO);
    return commentResponse;
  }
}
