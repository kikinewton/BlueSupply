package com.logistics.supply.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
public class Quotation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne private Supplier supplier;

  String quotationRef;

  boolean linkedToLpo;

  boolean expired;

  @OneToOne private RequestDocument requestDocument;

  @CreationTimestamp Date createdAt;
}
