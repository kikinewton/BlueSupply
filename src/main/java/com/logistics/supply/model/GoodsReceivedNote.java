package com.logistics.supply.model;


import com.fasterxml.jackson.annotation.JsonFormat;
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

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @CreationTimestamp
  LocalDateTime createdDate;

  @OneToOne private Invoice invoice;

  private BigDecimal invoiceAmountPayable;

  private int supplier;

  @Transient
  private Supplier finalSupplier;

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

  @Transient
  private List<Payment> paymentHistory;

  public GoodsReceivedNote() {}

  @PostLoad
  public void loadSupplier() {
    finalSupplier = invoice.getSupplier();
  }
}
