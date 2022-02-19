package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@JsonIgnoreProperties(
    value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class FloatOrder  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "created_by_id")
  private Employee createdBy;

  @CreationTimestamp
  private LocalDate createdDate;


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
  private String requestedByEmail;
  private String staffId;
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

  @Column
  boolean hasDocument;

  @ManyToOne
  @JoinColumn(name = "department_id")
  Department department;

  @Size(max = 6)
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

  @Override
  public String toString() {
    return "FloatOrder{" +
            "id=" + id +
            ", createdBy=" + createdBy +
            ", createdDate=" + createdDate +
            ", retired=" + retired +
            ", flagged=" + flagged +
            ", floatOrderRef='" + floatOrderRef + '\'' +
            ", requestedBy='" + requestedBy + '\'' +
            ", requestedByPhoneNo='" + requestedByPhoneNo + '\'' +
            ", amount=" + amount +
            ", description='" + description + '\'' +
            ", endorsementDate=" + endorsementDate +
            ", approvalDate=" + approvalDate +
            ", endorsement=" + endorsement +
            ", approval=" + approval +
            ", status=" + status +
            ", fundsReceived=" + fundsReceived +
            ", hasDocument=" + hasDocument +
            ", department=" + department +
            ", retirementDate=" + retirementDate +
            ", auditorRetirementApproval=" + auditorRetirementApproval +
            ", auditorRetirementApprovalDate=" + auditorRetirementApprovalDate +
            ", gmRetirementApproval=" + gmRetirementApproval +
            ", gmRetirementApprovalDate=" + gmRetirementApprovalDate +
            '}';
  }
}
