package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.listener.PettyCashCommentListener;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EntityListeners(PettyCashCommentListener.class)
public class PettyCashComment extends Comment {

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "petty_cash_id")
  PettyCash pettyCash;

  @Builder
  public PettyCashComment(
      long id,
      String description,
      boolean read,
      RequestProcess processWithComment,
      Employee employee,
      Date createdDate,
      Date updatedDate,
      PettyCash pettyCash) {
    super(id, description, read, processWithComment, employee, createdDate, updatedDate);
    this.pettyCash = pettyCash;
  }
}
