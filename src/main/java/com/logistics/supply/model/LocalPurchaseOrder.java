package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.annotation.ValidDescription;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(
    value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class LocalPurchaseOrder extends AbstractAuditable<Employee, Integer> {

  @ValidDescription
  private String comment;

  @Size(min = 1)
  @OneToMany private Set<RequestItem> requestItems;

  @Column(nullable = false, updatable = false)
  private Integer supplierId;

  private String lpoRef;

  @Future private Date deliveryDate;

  @UpdateTimestamp @JsonIgnore private Date updatedDate;

  @PostUpdate
  public void logAfterUpdate() {
    updatedDate = new Date();
  }
}
