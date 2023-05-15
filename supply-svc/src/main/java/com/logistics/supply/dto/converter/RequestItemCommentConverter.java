package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.RequestItemDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.interfaces.GenericConverter;
import com.logistics.supply.model.RequestItemComment;

@Component
public class RequestItemCommentConverter
    implements GenericConverter<RequestItemComment, CommentResponse<RequestItemDTO>> {

  @Override
  public CommentResponse<RequestItemDTO> apply(RequestItemComment requestItemComment) {
    CommentResponse<RequestItemDTO> commentResponse = new CommentResponse<>();
    BeanUtils.copyProperties(requestItemComment, commentResponse);
    EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(requestItemComment.getEmployee());
    commentResponse.setCommentBy(employeeMinorDTO);
    RequestItemDTO requestItemDTO = RequestItemDTO.toDto(requestItemComment.getRequestItem());
    commentResponse.setItem(requestItemDTO);
    return commentResponse;
  }
}
