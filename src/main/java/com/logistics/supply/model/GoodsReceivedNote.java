package com.logistics.supply.model;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @CreationTimestamp
  LocalDateTime createdDate;

  @OneToOne private Invoice invoice;

  private BigDecimal invoiceAmountPayable;

  private int supplier;

  @FutureOrPresent
  private Date paymentDate;

  @Transient
  private List<GoodsReceivedNoteComment> comments;

  @OneToMany
  @JoinColumn(name = "grn_id")
  private List<RequestItem> receivedItems;

  @UpdateTimestamp
  private Date updatedDate;

  private String grnRef;

  @OneToOne private LocalPurchaseOrder localPurchaseOrder;

  public GoodsReceivedNote() {}
}
