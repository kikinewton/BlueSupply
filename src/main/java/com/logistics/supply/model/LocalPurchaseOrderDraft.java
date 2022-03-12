package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.event.listener.LpoDraftEventListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Entity
@Data
@EntityListeners({AuditingEntityListener.class, LpoDraftEventListener.class})
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(
    value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new", "createdDate"})
public class LocalPurchaseOrderDraft extends AbstractAuditable<Employee, Integer> {

  @Size(min = 1)
  @OneToMany
  private Set<RequestItem> requestItems;

  @Column(nullable = false, updatable = false)
  private Integer supplierId;

  @OneToOne private Quotation quotation;

  @Future private Date deliveryDate;

  @CreationTimestamp private Date createdAt;

  @UpdateTimestamp @JsonIgnore private Date updatedDate;

  @OneToOne private Department department;

  @PostUpdate
  public void logAfterUpdate() {
    updatedDate = new Date();
  }

  @PrePersist
  private void setDepartment() {
    Optional<Department> dep = requestItems.stream().map(RequestItem::getUserDepartment).findAny();
    if (dep.isPresent()) department = dep.get();
  }
}
