package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.listener.FloatCommentListener;
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
@EntityListeners(FloatCommentListener.class)
public class FloatComment extends Comment {

  @ManyToOne
  @JoinColumn(name = "float")
  @JsonIgnore
  FloatOrder floats;

  @Builder
  public FloatComment(
      long id,
      String description,
      RequestProcess processWithComment,
      Employee employee,
      Date createdDate,
      Date updatedDate,
      FloatOrder floats) {
    super(id, description, processWithComment, employee, createdDate, updatedDate);
    this.floats = floats;
  }
}
