package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Immutable
public class FloatAgingAnalysis {
    @Id
    private String floatRef;
    @Column(name = "item_description")
    private String description;
    private String department;
    private String employee;
    private String requestedBy;
    private String requestedByPhoneNo;
    private String staffId;
    private BigDecimal estimatedAmount;
    private boolean retired;
    private int ageingValue;
    @Temporal(TemporalType.DATE)
    private Date createdDate;

}
