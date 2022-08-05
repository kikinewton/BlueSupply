package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Immutable
@Table(name = "float_payment_report")
public class FloatOrderPaymentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    private String floatOrderRef;
    private BigDecimal requestedAmount;
    private String floatType;
    private BigDecimal paidAmount;
    private Date requestedDate;
    private String requesterStaffId;
    private String department;
    private Date endorsementDate;
    private String endorsedBy;
    private Date approvalDate;
    private String approvedBy;
    private Date fundsAllocatedDate;
    private String accountOfficer;
    private Date retirementDate;
    private boolean retired;
}
