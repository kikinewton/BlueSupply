package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Immutable
@Table(name = "supplier_performance_view")
public class SupplierPerformance {

    @Id
    @Column(name = "supplier_id")
    private int supplierId;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "total_lpos")
    private long totalLpos;

    @Column(name = "avg_delivery_days")
    private int avgDeliveryDays;

    @Column(name = "payment_completion_rate")
    private BigDecimal paymentCompletionRate;
}
