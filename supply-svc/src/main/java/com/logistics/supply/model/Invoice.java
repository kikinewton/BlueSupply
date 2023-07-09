package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@ToString
@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Invoice extends AbstractAuditable<Employee, Integer> {

  public Invoice() {
    // TODO document why this constructor is empty
  }

  @Column(updatable = false, length = 30)
  private String invoiceNumber;

  @ManyToOne private Supplier supplier;

  @OneToOne private RequestDocument invoiceDocument;


}
