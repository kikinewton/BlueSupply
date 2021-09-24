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
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Floats extends AbstractAuditable<Employee, Integer> {

  @Column(nullable = false, unique = true)
  String floatRef;

  @ManyToOne
  @JoinColumn(name = "department_id")
  Department department;

  @NotBlank @PositiveOrZero BigDecimal estimatedUnitPrice;

  @NotBlank String itemDescription;

  @PositiveOrZero int quantity;


  @NotBlank
  String reason;

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

  boolean retired = false;

  @FutureOrPresent
  Date retirementDate;

  private boolean flagged = Boolean.FALSE;

  @Size(max = 4, min = 1)
  @ManyToMany(cascade = CascadeType.MERGE)
          @JoinTable(joinColumns = @JoinColumn(name = "float_id"), inverseJoinColumns = @JoinColumn(name = "support_document_id"))
  Set<RequestDocument> supportingDocuments;



}
