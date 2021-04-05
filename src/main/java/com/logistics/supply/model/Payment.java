package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStatus;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Data
@Entity
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NonNull
  @Column(unique = true)
  private String purchaseNumber;

  @OneToOne private Supplier supplier;

  @OneToOne private Invoice invoice;

  @OneToOne private ProcuredItem procuredItem;

  @OneToOne private LocalPurchaseOrder localPurchaseOrder;

  @OneToOne private GoodsReceivedNote goodsReceivedNote;

  @OneToOne private Employee accountOfficer;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @OneToOne private PaymentSchedule paymentSchedule;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @JsonIgnore @CreationTimestamp Date createdDate;

  @JsonIgnore Date updatedDate;

  @PostUpdate
  public void logAfterUpdate() {
    updatedDate = new Date();
  }
}
