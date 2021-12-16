package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.annotation.ValidName;
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
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

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
  String purpose;

  @JsonIgnore Date approvalDate;

  @JsonIgnore Date endorsementDate;

  @ManyToOne
  @JoinColumn(name = "department_id")
  Department department;

  String pettyCashRef;

  @Size(max = 4)
  @OneToMany
  Set<RequestDocument> supportingDocument;

  @Column(nullable = false, updatable = false)
  @ValidName
  private String name;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestApproval approval = RequestApproval.PENDING;

  @Column
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
}
