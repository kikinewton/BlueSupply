package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToOne;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
    value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class GoodsReceivedNote extends AbstractAuditable<Employee, Integer> {

  public GoodsReceivedNote() {
  }


  @OneToOne private Invoice invoice;

  private BigDecimal invoiceAmountPayable;

  private Integer supplier;

  String comment;

  //  @OneToOne private ProcuredItem procuredItem;

  @OneToOne private LocalPurchaseOrder localPurchaseOrder;


}
