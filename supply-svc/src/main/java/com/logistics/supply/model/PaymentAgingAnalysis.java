package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Immutable
@Table(name = "payment_aging_analysis")
public class PaymentAgingAnalysis {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "stage")
    private String stage;

    @Column(name = "days_outstanding")
    private int daysOutstanding;

    @Column(name = "aging_bucket")
    private String agingBucket;
}
