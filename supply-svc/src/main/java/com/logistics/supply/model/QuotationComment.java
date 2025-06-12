package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.listener.QuotationCommentListener;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Date;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
@EntityListeners(QuotationCommentListener.class)
public class QuotationComment extends Comment {

  @ManyToOne
  @JoinColumn(name = "quotation_id")
  Quotation quotation;

  @Builder
  public QuotationComment(
      long id,
      String description,
      RequestProcess processWithComment,
      Employee employee,
      Date createdDate,
      Date updatedDate,
      Quotation quotation) {
    super(id, description, processWithComment, employee, createdDate, updatedDate);
    this.quotation = quotation;
  }
}
