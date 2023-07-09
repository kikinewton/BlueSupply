package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.RequestItemDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.interfaces.GenericConverter;
import com.logistics.supply.model.RequestItemComment;

@Component
public class RequestItemCommentConverter
    implements GenericConverter<RequestItemComment, CommentResponse<RequestItemDto>> {

  @Override
  public CommentResponse<RequestItemDto> apply(RequestItemComment requestItemComment) {
    CommentResponse<RequestItemDto> commentResponse = new CommentResponse<>();
    BeanUtils.copyProperties(requestItemComment, commentResponse);
    EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(requestItemComment.getEmployee());
    commentResponse.setCommentBy(employeeMinorDTO);
    RequestItemDto requestItemDTO = RequestItemDto.toDto(requestItemComment.getRequestItem());
    commentResponse.setItem(requestItemDTO);
    return commentResponse;
  }
}
