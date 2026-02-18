package com.logistics.supply.model;


import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Immutable
public class PaymentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    private String supplier;
    private String invoiceNo;
    private String accountNumber;
    private String chequeNumber;
    private String purchaseNumber;
    private String paymentStatus;
    @Temporal(TemporalType.DATE)
    private Date paymentDueDate;
    @Temporal(TemporalType.DATE)
    private Date paymentDate;
    private BigDecimal payableAmount;
    private BigDecimal withholdingTaxPercentage;
    private BigDecimal withholdingTaxAmount;
    private String checkedByAuditor;
    private String verifiedByFm;
    private String approvedByGm;
}
