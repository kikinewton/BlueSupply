package com.logistics.supply.dto.converter;

import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.dto.PaymentDraftMinorDTO;
import com.logistics.supply.interfaces.GenericConverter;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.model.PaymentDraftComment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PaymentDraftCommentConverter
    implements GenericConverter<PaymentDraftComment, CommentResponse<PaymentDraftMinorDTO>> {

  @Override
  public CommentResponse<PaymentDraftMinorDTO> apply(PaymentDraftComment paymentDraftComment) {
    CommentResponse<PaymentDraftMinorDTO> commentResponse = new CommentResponse<>();
    PaymentDraft paymentDraft = paymentDraftComment.getPaymentDraft();
    BeanUtils.copyProperties(paymentDraft, commentResponse);
    PaymentDraftMinorDTO paymentDraftMinorDTO = PaymentDraftMinorDTO.toDto(paymentDraft);
    commentResponse.setItem(paymentDraftMinorDTO);
    EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(paymentDraftComment.getEmployee());
    commentResponse.setCommentBy(employeeMinorDTO);
    commentResponse.setId(paymentDraftComment.getId());
    commentResponse.setDescription(paymentDraftComment.getDescription());
    commentResponse.setProcessWithComment(paymentDraftComment.getProcessWithComment());
    return commentResponse;
  }
}
