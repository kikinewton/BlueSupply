package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
//@EntityListeners(AuditingEntityListener.class)
//@Table(
//    uniqueConstraints = {
//      @UniqueConstraint(
//          name = "idx_col1_col2",
//          columnNames = {"supplier_id", "request_document_id"})
//    })
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Quotation { // extends AbstractAuditable<Employee, Integer> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne private Supplier supplier;

  @OneToOne private RequestDocument requestDocument;
}
