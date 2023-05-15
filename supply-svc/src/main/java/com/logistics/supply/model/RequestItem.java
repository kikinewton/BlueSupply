package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.enums.*;
import com.logistics.supply.event.listener.RequestItemEventListener;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "request_item")
@SQLDelete(sql = "UPDATE request_item SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")
@EntityListeners(RequestItemEventListener.class)
public class RequestItem {

  @Enumerated(EnumType.STRING)
  @Column(length = 25)
  private PriorityLevel priorityLevel;

  @Column private Date approvalDate;

  @Column private Date endorsementDate;

  @Column(unique = true, length = 50)
  String requestItemRef;

  @Column(length = 50)
  @Enumerated(EnumType.STRING)
  private RequestReview requestReview;

  @CreationTimestamp private Date createdDate;

  @JsonIgnore @UpdateTimestamp private Date updatedDate;

  @Size(max = 4)
  @ManyToMany(cascade = {CascadeType.MERGE})
  @JoinTable(
      joinColumns = @JoinColumn(name = "request_item_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "quotation_id", referencedColumnName = "id"),
      name = "request_item_quotations")
  @Fetch(FetchMode.JOIN)
  private Set<Quotation> quotations = new HashSet<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotBlank
  @Column(nullable = false, length = 200)
  private String name;

  @Column(nullable = false, updatable = false, length = 200)
  @Enumerated(EnumType.STRING)
  private RequestReason reason;

  @Column(nullable = false, updatable = false, length = 200)
  @ValidDescription
  private String purpose;

  @Column(nullable = false)
  @Positive
  private Integer quantity;

  @Column @PositiveOrZero private BigDecimal unitPrice = BigDecimal.valueOf(0);
  @Column @PositiveOrZero private BigDecimal totalPrice = BigDecimal.valueOf(0);

  @Size(max = 3)
  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinTable(
      joinColumns = @JoinColumn(name = "request_id", nullable = false),
      inverseJoinColumns = @JoinColumn(name = "supplier_id", nullable = false))
  private Set<Supplier> suppliers;

  private Integer suppliedBy;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestApproval approval = RequestApproval.PENDING;

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

  @Column(length = 10)
  private String currency;

  private boolean deleted;

  @Transient private List<RequestItemComment> comment;

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
