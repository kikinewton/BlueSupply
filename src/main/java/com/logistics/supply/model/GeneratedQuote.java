package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Slf4j
@Data
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class GeneratedQuote extends AbstractAuditable<Employee, Integer> {

  @ManyToOne
  @JoinColumn(name = "supplier_id")
  Supplier supplierName;

  String phoneNo;
  String location;
  String deliveryDate;
  String productDescription;

  public GeneratedQuote() {}
}
