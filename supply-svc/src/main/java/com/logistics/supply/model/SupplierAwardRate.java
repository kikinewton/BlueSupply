package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Immutable
@Table(name = "supplier_award_rate_view")
public class SupplierAwardRate {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "quotations_submitted")
    private long quotationsSubmitted;

    @Column(name = "lpos_awarded")
    private long lposAwarded;

    @Column(name = "award_rate_pct")
    private BigDecimal awardRatePct;

    @Column(name = "total_lpo_value")
    private BigDecimal totalLpoValue;
}
