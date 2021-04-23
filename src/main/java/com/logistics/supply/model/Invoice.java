package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.util.CommonHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.util.Date;

import static com.logistics.supply.util.CommonHelper.*;

@Slf4j
@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Invoice extends AbstractAuditable<Employee, Integer> {

  @Column(updatable = false)
  private String invoiceNumber;

  @PositiveOrZero private Integer numberOfDaysToPayment = 0;

  @Column(updatable = false)
  private Date paymentDate;

  @ManyToOne private Supplier supplier;

  @OneToOne private RequestDocument invoiceDocument;

  @PrePersist
  public void logNewInvoiceAttempt() {
    log.info("Attempting to add new invoice with number: " + invoiceNumber);
    paymentDate = calculatePaymentDate(numberOfDaysToPayment);
  }
}
