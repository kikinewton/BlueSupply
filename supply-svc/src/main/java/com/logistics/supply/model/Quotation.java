package com.logistics.supply.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@SQLDelete(sql = "UPDATE quotation SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")
public class Quotation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @OneToOne private Supplier supplier;

  @Column(length = 30)
  private String quotationRef;

  private boolean linkedToLpo;

  private boolean expired;

  private boolean deleted;

  private boolean reviewed;

  @OneToOne private RequestDocument requestDocument;

  @ManyToOne
  @JoinColumn(name = "employee_id")
  private Employee createdBy;

  @CreationTimestamp private Date createdAt;
}
