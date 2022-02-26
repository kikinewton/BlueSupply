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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "request_item")
@EntityListeners(RequestItemEventListener.class)
public class RequestItem {

  @Enumerated(EnumType.STRING)
  @Column
  PriorityLevel priorityLevel;

  Date approvalDate;

  @Column Date endorsementDate;

  @Column(unique = true)
  String requestItemRef;

  @Column(columnDefinition = "CHECK(status is not null)")
  @Enumerated(EnumType.STRING)
  RequestReview requestReview;

  @CreationTimestamp Date createdDate;

  @JsonIgnore @UpdateTimestamp Date updatedDate;

  @Size(max = 4)
  @ManyToMany(cascade = CascadeType.MERGE)
  @JoinTable(
      joinColumns = @JoinColumn(name = "request_item_id"),
      inverseJoinColumns = @JoinColumn(name = "quotation_id"))
  Set<Quotation> quotations;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotBlank
  @Column(nullable = false)
  private String name;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private RequestReason reason;

  @Column(nullable = false, updatable = false)
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
