package com.logistics.supply.model;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class GoodsReceivedNote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  boolean approvedByHod;
  Date dateOfApprovalByHod;
  Integer employeeHod;

  int employeeGm;
  Date dateOfApprovalByGm;
  boolean approvedByGm;

  @ManyToOne
  @JoinColumn(name = "created_by_id")
  Employee createdBy;

  @CreationTimestamp
  LocalDate createdDate;

  @OneToOne private Invoice invoice;

  private BigDecimal invoiceAmountPayable;

  private int supplier;

  @OneToMany
  @JoinColumn(name = "grn_id")
  private List<RequestItem> receivedItems;

  @OneToOne private LocalPurchaseOrder localPurchaseOrder;

  public GoodsReceivedNote() {}
}
