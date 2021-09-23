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
import java.math.BigDecimal;
import java.util.Date;

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

  @ManyToOne
  @JoinColumn(name = "employee_id")
  Employee employee;

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

  @FutureOrPresent
  Date retirementDate;

  private boolean flagged = Boolean.FALSE;
}
