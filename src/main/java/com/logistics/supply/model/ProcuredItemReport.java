package com.logistics.supply.model;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Immutable
public class ProcuredItemReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    private String requestItemRef;
    private String name;
    private String reason;
    private int quantity;
    private String purpose;
    private String requestedBy;
    private String requestedByEmail;
    private String userDepartment;
    private String requestCategory;
    private String suppliedBy;
    private BigDecimal totalPrice;
    private Date createdDate;
    private Date grnIssuedDate;

}
