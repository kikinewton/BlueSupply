package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

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

    @OneToMany @JoinColumn(name = "grn_id")
    private Set<RequestItem> receivedItems;

    Boolean reviewByHOD;

  @OneToOne private LocalPurchaseOrder localPurchaseOrder;


}
