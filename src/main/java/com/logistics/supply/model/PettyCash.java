package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.annotation.ValidName;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;

import javax.persistence.*;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
public class PettyCash extends AbstractAuditable<Employee, Integer> {

  @Positive BigDecimal amount;

  @Column(nullable = false, updatable = false)
  @ValidName
  private String name;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  String purpose;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestApproval approval = RequestApproval.PENDING;

  @Column
  @Enumerated(EnumType.STRING)
  private EndorsementStatus endorsement = EndorsementStatus.PENDING;

  @JsonIgnore
  Date approvalDate;

  @JsonIgnore Date endorsementDate;


  @ManyToOne
  @JoinColumn(name = "department_id")
  Department department;

  String pettyCashRef;

  Set<RequestDocument> supportingDocument;

}
