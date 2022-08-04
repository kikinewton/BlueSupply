package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Immutable
@Table(name = "grn_report")
public class GrnReport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private long id;

  private String grnRef;
  private String issuer;
  private boolean approvedByHod;
  private BigDecimal invoiceAmountPayable;
  private String requestItem;
  private String supplier;
  private String invoiceNumber;
  private LocalDate dateReceived;
}
