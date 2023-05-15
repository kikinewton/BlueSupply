package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
public class PaymentSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private BigDecimal amount;

  @ManyToOne
  @JoinColumn(name = "supplier_id")
  private Supplier supplier;

  @FutureOrPresent private Date dueDate;

  @JsonIgnore @CreationTimestamp private Date creationDate;

  @JsonIgnore @UpdateTimestamp private Date updatedDate;
}
