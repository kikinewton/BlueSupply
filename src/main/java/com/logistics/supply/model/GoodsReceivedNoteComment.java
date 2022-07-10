package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.RequestProcess;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GoodsReceivedNoteComment extends Comment {

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "goods_received_note_id")
  GoodsReceivedNote goodsReceivedNote;

  @Builder
  public GoodsReceivedNoteComment(
      long id,
      String description,
      RequestProcess processWithComment,
      Employee employee,
      Date createdDate,
      Date updatedDate,
      GoodsReceivedNote goodsReceivedNote) {
    super(id, description, processWithComment, employee, createdDate, updatedDate);
    this.goodsReceivedNote = goodsReceivedNote;
  }
}
