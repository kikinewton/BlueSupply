package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logistics.supply.dto.PaymentDTO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class GoodsReceivedNote {

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
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @OneToOne private Invoice invoice;

  private BigDecimal invoiceAmountPayable;

  private int supplier;

  @Transient private Supplier finalSupplier;

  @FutureOrPresent private Date paymentDate;

  @Transient private List<GoodsReceivedNoteComment> comments;

  @OneToMany
  @JoinColumn(name = "grn_id")
  private List<RequestItem> receivedItems;

  @UpdateTimestamp private Date updatedDate;

  private String grnRef;

  @Transient private boolean hasPendingPaymentDraft;

  @OneToOne private LocalPurchaseOrder localPurchaseOrder;

  @Transient private List<PaymentDTO> paymentHistory = new ArrayList<>();

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
        + ", dateOfApprovalByGm="
        + dateOfApprovalByGm
        + ", approvedByGm="
        + approvedByGm
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
        + ", comments="
        + comments
        + ", receivedItems="
        + receivedItems
        + ", updatedDate="
        + updatedDate
        + ", grnRef='"
        + grnRef
        + ", paymentHistory="
        + paymentHistory
        + '}';
  }

  @PostLoad
  public void loadSupplier() {
    finalSupplier = invoice.getSupplier();
  }
}
