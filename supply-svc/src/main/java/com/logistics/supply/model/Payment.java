package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStage;
import com.logistics.supply.enums.PaymentStatus;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Getter
@Setter
@ToString
@Slf4j
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE payment SET deleted = true WHERE id=?")
@SQLRestriction("deleted=false")
@JsonIgnoreProperties(value = {"lastModifiedDate", "lastModifiedBy", "new"})
public class Payment extends AbstractAuditable<Employee, Integer> {

  @NonNull
  @Column(unique = true, updatable = false, length = 20)
  private String purchaseNumber;

  @ManyToOne private GoodsReceivedNote goodsReceivedNote;

  @Column(scale = 3)
  @PositiveOrZero
  private BigDecimal paymentAmount;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Column(unique = true, length = 30)
  private String chequeNumber;

  @Column(length = 50)
  private String bank;

  private Boolean approvalFromAuditor;

  private Date approvalByAuditorDate;

  @Column(nullable = false, updatable = false, scale = 3)
  @PositiveOrZero
  private BigDecimal withholdingTaxAmount;

  @Column(nullable = false, scale = 3)
  @PositiveOrZero
  private BigDecimal withholdingTaxPercentage;

  private Boolean approvalFromGM;
  private Boolean approvalFromFM;
  private Date approvalByGMDate;
  private Date approvalByFMDate;
  private Integer employeeFmId;
  private Integer employeeGmId;
  private Integer employeeAuditorId;
  private boolean deleted = false;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private PaymentStage stage = PaymentStage.DRAFT;

  @PrePersist
  private void calculateWithholdingTax() {
    BigDecimal rate = withholdingTaxPercentage.divide(BigDecimal.valueOf(100f));
    BigDecimal invoiceAmountPayable = goodsReceivedNote.getInvoiceAmountPayable();
    withholdingTaxAmount = invoiceAmountPayable.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    withholdingTaxPercentage = rate;
  }
}
