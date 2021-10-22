package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.event.GRNListener;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class, GRNListener.class})
@JsonIgnoreProperties(value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class GoodsReceivedNote extends AbstractAuditable<Employee, Integer> {

  Boolean reviewByHOD;
  @OneToOne private Invoice invoice;

  private BigDecimal invoiceAmountPayable;

  private Integer supplier;

  @OneToMany
  @JoinColumn(name = "grn_id")
  private Set<RequestItem> receivedItems;

  @OneToOne private LocalPurchaseOrder localPurchaseOrder;

  public GoodsReceivedNote() {}
}
