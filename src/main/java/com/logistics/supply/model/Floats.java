package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "float")
public class Floats  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true)
  String floatRef;

  @ManyToOne
  @JoinColumn(name = "department_id")
  Department department;

  @NotBlank @PositiveOrZero BigDecimal estimatedUnitPrice;

  @NotBlank String itemDescription;

  @PositiveOrZero int quantity;

  boolean isProduct;

  @FutureOrPresent
  @JsonIgnore Date endorsementDate;

  @FutureOrPresent
  @JsonIgnore Date approvalDate;

  @Column
  @Enumerated(EnumType.STRING)
  private EndorsementStatus endorsement = EndorsementStatus.PENDING;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestApproval approval = RequestApproval.PENDING;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  String purpose;

  boolean retired = false;

  boolean fundsReceived;

  @FutureOrPresent
  Date retirementDate;

  private boolean flagged = Boolean.FALSE;

  @Size(max = 4)
  @ManyToMany(cascade = CascadeType.MERGE)
          @JoinTable(joinColumns = @JoinColumn(name = "float_id"), inverseJoinColumns = @JoinColumn(name = "document_id"))
  Set<RequestDocument> supportingDocument;

  @CreationTimestamp
  Date createdDate;

  @UpdateTimestamp
  Date updatedDate;

  @ManyToOne
  @JoinColumn(name = "created_by_id")
  Employee createdBy;

  Boolean hodRetirementApproval;
  Date hodRetirementApprovalDate;

  Boolean auditorRetirementApproval;
  Date auditorRetirementApprovalDate;

  Boolean gmRetirementApproval;
  Date gmRetirementApprovalDate;



}
