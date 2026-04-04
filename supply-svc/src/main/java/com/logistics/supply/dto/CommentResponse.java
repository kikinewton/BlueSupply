package com.logistics.supply.dto;

import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.model.Comment;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CommentResponse<T> {
  private long id;
  private String description;
  private boolean read;
  private T item;
  private RequestProcess processWithComment;
  private Date createdDate;
  private Date updatedDate;
  private EmployeeMinorDto commentBy;

  public static <T> CommentResponse<T> from(Comment comment, EmployeeMinorDto commentBy, T item) {
    CommentResponse<T> r = new CommentResponse<>();
    r.id = comment.getId();
    r.description = comment.getDescription();
    r.processWithComment = comment.getProcessWithComment();
    r.createdDate = comment.getCreatedDate();
    r.updatedDate = comment.getUpdatedDate();
    r.commentBy = commentBy;
    r.item = item;
    return r;
  }
}
