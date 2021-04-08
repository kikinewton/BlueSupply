package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Entity
@Slf4j
@Data
public class RequestItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private RequestReason reason;

  @Column(nullable = false)
  private String purpose;

  @Column(nullable = false)
  @PositiveOrZero
  private Integer quantity = 0;

  @Column @PositiveOrZero private BigDecimal unitPrice = BigDecimal.valueOf(0);

  @Column @PositiveOrZero private BigDecimal totalPrice = BigDecimal.valueOf(0);

  @ManyToMany(cascade = CascadeType.MERGE)
  @JoinTable(
      joinColumns = @JoinColumn(name = "request_id"),
      inverseJoinColumns = @JoinColumn(name = "supplier_id"))
  private Set<Supplier> suppliers;

  private Integer suppliedBy;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestApproval approval = RequestApproval.PENDING;

  @JsonIgnore Date approvalDate;

  @JsonIgnore Date endorsementDate;

  @Column
  @Enumerated(EnumType.STRING)
  private EndorsementStatus endorsement = EndorsementStatus.PENDING;

  @Column private Date requestDate = new Date();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @OneToOne
  @JoinColumn(name = "user_department")
  private Department userDepartment;

  @OneToOne
  @JoinColumn(name = "request_category")
  private RequestCategory requestCategory;

  @Enumerated(EnumType.STRING)
  private RequestType requestType;

  @JsonIgnore Date createdDate = new Date();

  @JsonIgnore @UpdateTimestamp Date updatedDate;

  @ManyToMany(cascade = CascadeType.MERGE)
  @JoinTable(
      joinColumns = @JoinColumn(name = "request_item_id"),
      inverseJoinColumns = @JoinColumn(name = "quotation_id"))
  Set<Quotation> quotations;

  @PreUpdate
  public void preUpdate() {
    log.info("Attempting to update requestItem: " + id);
  }

  @PrePersist
  public void logNewRequestItemAttempt() {
    log.info("Attempting to add new request with name: " + name);
  }

  @PostPersist
  public void logNewRequestItemAdded() {
    log.info("Added requestItem '" + name + "' with ID: " + id);
  }

  @PreRemove
  public void logRequestItemRemovalAttempt() {
    log.info("Attempting to delete requestItem: " + id);
  }

  @PostRemove
  public void logRequestItemRemoval() {
    log.info("Deleted requestItem: " + id);
  }

  @PostUpdate
  public void logRequestItemUpdate() {
    log.info("Updated requestItem: " + id);
  }
}
