package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "float_grn_comment")
public class FloatGrnComment extends Comment {

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "float_grn_id")
  FloatGRN floatGRN;

  @Builder
  public FloatGrnComment(
      long id,
      String description,
      RequestProcess processWithComment,
      Employee employee,
      Date createdDate,
      Date updatedDate,
      FloatGRN floatGRN) {
    super(id, description, processWithComment, employee, createdDate, updatedDate);
    this.floatGRN = floatGRN;
  }
}
