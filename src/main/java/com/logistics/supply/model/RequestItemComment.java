package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.listener.RequestItemCommentListener;
import lombok.*;

import javax.persistence.*;
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
      boolean read,
      RequestProcess processWithComment,
      Employee employee,
      Date createdDate,
      Date updatedDate,
      RequestItem requestItem) {
    super(id, description, read, processWithComment, employee, createdDate, updatedDate);
    this.requestItem = requestItem;
  }
}
