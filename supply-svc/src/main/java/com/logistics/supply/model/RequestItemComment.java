package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.listener.RequestItemCommentListener;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@EntityListeners(RequestItemCommentListener.class)
public class RequestItemComment extends Comment {
  @ManyToOne
  @JoinColumn(name = "request_item_id")
  RequestItem requestItem;

  @Builder
  public RequestItemComment(
      long id,
      String description,
      RequestProcess processWithComment,
      Employee employee,
      Date createdDate,
      Date updatedDate,
      RequestItem requestItem) {
    super(id, description, processWithComment, employee, createdDate, updatedDate);
    this.requestItem = requestItem;
  }
}
