package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@JsonIgnoreProperties(
    value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class FloatOrder extends AbstractAuditable<Employee, Integer> {

  @OneToMany(
      mappedBy = "floatOrder",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private Set<Floats> floats = new HashSet<>();

  private boolean retired;

  // flag is to indicate that a float that has received funds hasn't been retired after 14 days
  private boolean flagged;

  private String floatOrderRef;
  private String requestedBy;
  private String requestedByPhoneNo;
  private BigDecimal amount;
  private String description;


  @JsonIgnore
  Date endorsementDate;

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

  @Column
  boolean fundsReceived;

  @ManyToOne
  @JoinColumn(name = "department_id")
  Department department;

  @Size(max = 4)
  @ManyToMany(cascade = CascadeType.MERGE)
  @JoinTable(joinColumns = @JoinColumn(name = "float_order_id"), inverseJoinColumns = @JoinColumn(name = "document_id"))
  Set<RequestDocument> supportingDocument;

  @FutureOrPresent
  Date retirementDate;

  Boolean auditorRetirementApproval;
  Date auditorRetirementApprovalDate;

  Boolean gmRetirementApproval;
  Date gmRetirementApprovalDate;

  public FloatOrder(Set<Floats> floats) {
    this.floats = floats;
  }

  public void addFloat(Floats _floats) {
    this.floats.add(_floats);
    _floats.setFloatOrder(this);
  }
}
