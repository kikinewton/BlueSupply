package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.listener.QuotationCommentListener;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
      boolean read,
      RequestProcess processWithComment,
      Employee employee,
      Date createdDate,
      Date updatedDate,
      Quotation quotation) {
    super(id, description, read, processWithComment, employee, createdDate, updatedDate);
    this.quotation = quotation;
  }
}
