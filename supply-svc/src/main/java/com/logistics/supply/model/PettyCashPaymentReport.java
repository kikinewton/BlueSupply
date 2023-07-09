package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Immutable
public class PettyCashPaymentReport {
    @Id
    private String pettyCashRef;
    private Date paymentDate;
    private String pettyCashDescription;
    private String purpose;
    private int quantity;
    private BigDecimal amount;
    private BigDecimal totalCost;
    private String requestedBy;
    private String requestedByEmail;
    private String department;
    private String paidBy;

}
