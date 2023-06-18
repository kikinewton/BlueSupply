package com.logistics.supply.dto;

import com.logistics.supply.enums.RequestProcess;
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
}
