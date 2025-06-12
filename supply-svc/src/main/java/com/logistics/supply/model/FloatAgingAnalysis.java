package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Immutable
public class FloatAgingAnalysis {
    @Id
    private String floatRef;
    private String description;
    private String department;
    private String employee;
    private String requestedBy;
    private String requestedByPhoneNo;
    private String staffId;
    private BigDecimal estimatedAmount;
    private boolean retired;
    private int ageingValue;
    private Date createdDate;

}
