package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

@Setter
@Getter
@ToString
@Entity
@NoArgsConstructor
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
