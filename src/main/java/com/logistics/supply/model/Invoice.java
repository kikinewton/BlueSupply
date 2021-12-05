package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@ToString
@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Invoice extends AbstractAuditable<Employee, Integer> {

  public Invoice() {
  }

  @Column(updatable = false)
  private String invoiceNumber;

  @ManyToOne private Supplier supplier;

  @OneToOne private RequestDocument invoiceDocument;


}
