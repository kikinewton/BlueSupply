package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStatus;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Payment extends AbstractAuditable<Employee, Integer> {

  @NonNull
  @Column(unique = true)
  private String purchaseNumber;

  @OneToOne private Supplier supplier;

  @OneToOne private Invoice invoice;

  @OneToOne private ProcuredItem procuredItem;

  @OneToOne private LocalPurchaseOrder localPurchaseOrder;

  @OneToOne private GoodsReceivedNote goodsReceivedNote;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @OneToOne private PaymentSchedule paymentSchedule;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;
}
