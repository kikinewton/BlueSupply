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

  boolean approvedByHod;
  Date dateOfApprovalByHod;
  Integer employeeHod;

  @ManyToOne
  @JoinColumn(name = "created_by_id")
  Employee createdBy;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @CreationTimestamp
  LocalDateTime createdDate;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @OneToOne private Invoice invoice;

  private BigDecimal invoiceAmountPayable;

  private int supplier;

  private Integer procurementManagerId;

  @Transient private Supplier finalSupplier;

  @FutureOrPresent private Date paymentDate;

  @OneToMany
  @JoinColumn(name = "grn_id")
  private List<RequestItem> receivedItems;

  @UpdateTimestamp private Date updatedDate;

  @Column(length = 20)
  private String grnRef;

  @Transient private boolean hasPendingPaymentDraft;

  @OneToOne private LocalPurchaseOrder localPurchaseOrder;

  public GoodsReceivedNote() {}

  @Override
  public String toString() {
    return "GoodsReceivedNote{"
        + "id="
        + id
        + ", approvedByHod="
        + approvedByHod
        + ", dateOfApprovalByHod="
        + dateOfApprovalByHod
        + ", createdBy="
        + createdBy
        + ", createdDate="
        + createdDate
        + ", invoice="
        + invoice
        + ", invoiceAmountPayable="
        + invoiceAmountPayable
        + ", supplier="
        + supplier
        + ", hasPendingPaymentDraft= "
        + hasPendingPaymentDraft
        + ", finalSupplier="
        + finalSupplier
        + ", paymentDate="
        + paymentDate
        + ", receivedItems="
        + receivedItems
        + ", updatedDate="
        + updatedDate
        + ", grnRef='"
        + grnRef
        + '}';
  }

  @PostLoad
  public void loadSupplier() {
    finalSupplier = invoice.getSupplier();
  }
}
