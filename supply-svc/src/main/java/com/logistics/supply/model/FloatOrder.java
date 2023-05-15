package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.dto.DepartmentDTO;
import com.logistics.supply.dto.EmployeeMinorDTO;
import com.logistics.supply.dto.FloatDTO;
import com.logistics.supply.enums.RequestApproval;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Size;

import com.logistics.supply.dto.MinorDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.FloatType;
import com.logistics.supply.enums.RequestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@NoArgsConstructor
@SQLDelete(sql = "UPDATE float_order SET deleted = true  WHERE id = ?")
@Where(clause = "deleted = false")
@JsonIgnoreProperties(value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class FloatOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "created_by_id")
  private Employee createdBy;

  @CreationTimestamp private LocalDate createdDate;

  @OneToMany(
      mappedBy = "floatOrder",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private Set<Floats> floats = new HashSet<>();

  private boolean retired;

  // flag is to indicate that a float that has received funds hasn't been retired after 14 days
  private boolean flagged;

  private boolean deleted;

  @Column(length = 20)
  private String floatOrderRef;

  @Column(length = 30)
  private String requestedBy;

  @Column(length = 20)
  private String requestedByPhoneNo;

  @Column(length = 30)
  private String requestedByEmail;

  @Column(length = 20)
  private String staffId;

  private BigDecimal amount;
  private String description;

  @JsonIgnore Date endorsementDate;

  @JsonIgnore Date approvalDate;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private EndorsementStatus endorsement = EndorsementStatus.PENDING;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private RequestApproval approval = RequestApproval.PENDING;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  @Column(length = 10)
  @Enumerated(EnumType.STRING)
  private FloatType floatType;
  @Column private boolean fundsReceived;

  @Column private boolean hasDocument;

  @ManyToOne
  @JoinColumn(name = "department_id")
  private Department department;

  @Size(max = 6)
  @ManyToMany(cascade = CascadeType.MERGE)
  @JoinTable(
      joinColumns = @JoinColumn(name = "float_order_id"),
      inverseJoinColumns = @JoinColumn(name = "document_id"))
  private Set<RequestDocument> supportingDocument;

  @FutureOrPresent private Date retirementDate;

  private Boolean auditorRetirementApproval;
  private Date auditorRetirementApprovalDate;

  private Boolean gmRetirementApproval;
  private Date gmRetirementApprovalDate;

  private Integer endorsedBy;
  private Integer approvedBy;

  @PrePersist
  public void addFloatType(){
    if (this.floats.isEmpty()) {
      this.setFloatType(FloatType.SERVICE);
    } else {
      this.setFloatType(FloatType.GOODS);
    }
  }

  public FloatOrder(Set<Floats> floats) {
    this.floats = floats;
  }

  public void addFloat(Floats _floats) {
    this.floats.add(_floats);
    _floats.setFloatOrder(this);
  }

  @Override
  public String toString() {
    return "FloatOrder{"
        + "id="
        + id
        + ", createdBy="
        + createdBy
        + ", createdDate="
        + createdDate
        + ", retired="
        + retired
        + ", flagged="
        + flagged
        + ", floatOrderRef='"
        + floatOrderRef
        + '\''
        + ", requestedBy='"
        + requestedBy
        + '\''
        + ", requestedByPhoneNo='"
        + requestedByPhoneNo
        + '\''
        + ", amount="
        + amount
        + ", description='"
        + description
        + '\''
        + ", endorsementDate="
        + endorsementDate
        + ", approvalDate="
        + approvalDate
        + ", endorsement="
        + endorsement
        + ", approval="
        + approval
        + ", status="
        + status
        + ", fundsReceived="
        + fundsReceived
        + ", hasDocument="
        + hasDocument
        + ", department="
        + department
        + ", retirementDate="
        + retirementDate
        + ", auditorRetirementApproval="
        + auditorRetirementApproval
        + ", auditorRetirementApprovalDate="
        + auditorRetirementApprovalDate
        + ", gmRetirementApproval="
        + gmRetirementApproval
        + ", gmRetirementApprovalDate="
        + gmRetirementApprovalDate
        + '}';
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static final class FloatOrderDTO extends MinorDTO {
    private String staffId;
    private BigDecimal amount;
    private String description;
    private String floatOrderRef;
    private LocalDate createdDate;
    private boolean fundsReceived;
    private Date retirementDate;
    private DepartmentDTO department;
    private RequestApproval approval;
    private RequestStatus status;
    private EmployeeMinorDTO createdBy;
    private Set<FloatDTO> floats;

    public static FloatOrderDTO toDto(FloatOrder floatOrder) {
      FloatOrderDTO floatOrderDTO = new FloatOrderDTO();
      BeanUtils.copyProperties(floatOrder, floatOrderDTO);
      DepartmentDTO departmentDTO = DepartmentDTO.toDto(floatOrder.getDepartment());
      floatOrderDTO.setDepartment(departmentDTO);
      EmployeeMinorDTO employeeMinorDTO = EmployeeMinorDTO.toDto(floatOrder.getCreatedBy());
      floatOrderDTO.setCreatedBy(employeeMinorDTO);
      Set<Floats> floats1 = floatOrder.getFloats();
      if (floats1 != null && !floats1.isEmpty()) {
        Set<FloatDTO> floatDTOS =
            floats1.stream().map(f -> FloatDTO.toDto(f)).collect(Collectors.toSet());
        floatOrderDTO.setFloats(floatDTOS);
      }
      return floatOrderDTO;
    }
  }
}
