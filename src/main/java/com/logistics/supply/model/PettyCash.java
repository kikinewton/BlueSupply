package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PettyCash implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Positive BigDecimal amount;

  @Positive int quantity;

  @Column(length = 20)
  String purpose;

  @JsonIgnore Date approvalDate;

  @JsonIgnore Date endorsementDate;

  @ManyToOne
  @JoinColumn(name = "department_id")
  Department department;

  @Column(length = 20)
  String pettyCashRef;

  @Column(length = 20)
  String staffId;

  @Column(nullable = false, length = 100)
  @NotBlank
  private String name;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private RequestApproval approval = RequestApproval.PENDING;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private EndorsementStatus endorsement = EndorsementStatus.PENDING;

  Boolean paid;

  @CreationTimestamp
  Date createdDate;

  @UpdateTimestamp
  Date updatedDate;

  @ManyToOne
  @JoinColumn(name = "created_by")
  Employee createdBy;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "petty_cash_order_id")
  PettyCashOrder pettyCashOrder;
}
