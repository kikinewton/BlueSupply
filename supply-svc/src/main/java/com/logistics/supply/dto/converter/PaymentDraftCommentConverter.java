package com.logistics.supply.dto.converter;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.dto.PaymentDraftMinorDto;
import com.logistics.supply.interfaces.GenericConverter;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.model.PaymentDraftComment;

@Component
public class PaymentDraftCommentConverter
    implements GenericConverter<PaymentDraftComment, CommentResponse<PaymentDraftMinorDto>> {

  @Override
  public CommentResponse<PaymentDraftMinorDto> apply(PaymentDraftComment paymentDraftComment) {
    CommentResponse<PaymentDraftMinorDto> commentResponse = new CommentResponse<>();
    PaymentDraft paymentDraft = paymentDraftComment.getPaymentDraft();
    BeanUtils.copyProperties(paymentDraft, commentResponse);
    PaymentDraftMinorDto paymentDraftMinorDTO = PaymentDraftMinorDto.toDto(paymentDraft);
    commentResponse.setItem(paymentDraftMinorDTO);
    EmployeeMinorDto employeeMinorDTO = EmployeeMinorDto.toDto(paymentDraftComment.getEmployee());
    commentResponse.setCommentBy(employeeMinorDTO);
    commentResponse.setId(paymentDraftComment.getId());
    commentResponse.setDescription(paymentDraftComment.getDescription());
    commentResponse.setProcessWithComment(paymentDraftComment.getProcessWithComment());
    return commentResponse;
  }
}
