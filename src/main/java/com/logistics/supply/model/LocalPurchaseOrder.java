package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(
        value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class LocalPurchaseOrder extends AbstractAuditable<Employee, Integer> {

  private String comment;

  @OneToMany private Set<RequestItem> requestItems;

  @Column(nullable = false, updatable = false)
  private int supplierId;

  @JsonIgnore private Date updatedDate;


  @PostUpdate
  public void logAfterUpdate() {
    updatedDate = new Date();
  }
}
