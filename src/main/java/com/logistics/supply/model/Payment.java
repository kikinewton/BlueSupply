package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStatus;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@Slf4j
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Payment extends AbstractAuditable<Employee, Integer> {

  @NonNull
  @Column(unique = true, updatable = false)
  private String purchaseNumber;

  @OneToOne private GoodsReceivedNote goodsReceivedNote;

  @Column(updatable = false)
  @PositiveOrZero
  private BigDecimal paymentAmount;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Column(updatable = false, nullable = false)
  private String chequeNumber;

  @Column(updatable = false, nullable = false)
  private String bank;

  @Column(updatable = false)
  private Boolean approvalFromAuditor;

  private Boolean approvalFromGM;
  private Boolean approvalFromFM;
  private Date approvalByGMDate;
  private Date approvalByFMDate;
  private Integer paymentDraftId;
  private Integer employeeFmId;
  private Integer employeeGmId;
  private Integer employeeAuditorId;

}
