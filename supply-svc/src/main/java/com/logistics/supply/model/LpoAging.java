package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Immutable
@Table(name = "lpo_aging_view")
public class LpoAging {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "lpo_ref")
    private String lpoRef;

    @Column(name = "department")
    private String department;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "days_without_grn")
    private int daysWithoutGrn;

    @Column(name = "aging_bucket")
    private String agingBucket;
}
