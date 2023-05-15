package com.logistics.supply.model;

import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@Slf4j
@Entity
@SQLDelete(sql = "UPDATE payment_draft SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@NoArgsConstructor
public class PaymentDraft {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(unique = true, nullable = false, length = 20)
  private String purchaseNumber;

  @OneToOne private GoodsReceivedNote goodsReceivedNote;

  @PositiveOrZero private BigDecimal paymentAmount;

  @Column(nullable = false, updatable = false, scale = 3)
  @PositiveOrZero
  private BigDecimal withholdingTaxAmount;

  @Column(scale = 3)
  @PositiveOrZero
  private BigDecimal withholdingTaxPercentage;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Column(updatable = false, length = 20)
  private String chequeNumber;

  @Column(updatable = false, length = 50)
  private String bank;
  private Boolean approvalFromAuditor;

  private Date approvalByAuditorDate;
  private Boolean approvalFromGM;

  private Boolean approvalFromFM;

  private Date approvalByGMDate;

  private Date approvalByFMDate;

  @CreationTimestamp private Date createdDate;

  @ManyToOne
  @JoinColumn(name = "created_by_id")
  Employee createdBy;

  Integer employeeFmId;
  Integer employeeGmId;
  Integer employeeAuditorId;
  boolean deleted;

  @PrePersist
  public void calculateWithHoldingTax() {
    withholdingTaxPercentage = withholdingTaxPercentage.divide(BigDecimal.valueOf(100f));
    BigDecimal invoiceAmountPayable = goodsReceivedNote.getInvoiceAmountPayable();
    withholdingTaxAmount = invoiceAmountPayable.multiply(withholdingTaxPercentage).setScale(2, RoundingMode.HALF_UP);
  }

  @PreUpdate
  public void setDeletedWhenApprovedByGM() {
    if (Objects.nonNull(approvalFromGM)
        && approvalFromGM
        && Objects.nonNull(approvalFromFM)
        && approvalFromFM
        && Objects.nonNull(approvalFromAuditor)
        && approvalFromAuditor) {
      this.deleted = true;
    }
  }
}
