package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import com.logistics.supply.event.listener.PaymentDraftCommentListener;
import java.util.Date;

@Setter
@Getter
@ToString
@Entity
@NoArgsConstructor
@EntityListeners(PaymentDraftCommentListener.class)
public class PaymentDraftComment extends Comment {

  @ManyToOne
  @JoinColumn(name = "payment_draft_id")
  PaymentDraft paymentDraft;

  @Builder
  public PaymentDraftComment(
      long id,
      String description,
      RequestProcess processWithComment,
      Employee employee,
      Date createdDate,
      Date updatedDate,
      PaymentDraft paymentDraft) {
    super(id, description, processWithComment, employee, createdDate, updatedDate);
    this.paymentDraft = paymentDraft;
  }
}
