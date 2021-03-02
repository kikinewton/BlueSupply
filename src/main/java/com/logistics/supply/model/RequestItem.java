package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.enums.RequestStatus;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.util.Date;

@Entity
@Data
public class RequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestReason reason;

    @Column
    private String purpose;

    @Column(nullable = false)
    @PositiveOrZero
    private Integer quantity;

    @Column(nullable = false)
    @PositiveOrZero
    private Float unitPrice;

    @Column(nullable = false)
    @PositiveOrZero
    private Float amount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestApproval approval;

    private Date approvalDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Date endorsementDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EndorsementStatus endorsement;

    @Column(nullable = false)
    private Date requestDate = new Date();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="employee_id")
    private Employee employee;


    Date createdDate;

    Date updatedDate;


    @PrePersist
    public void prePersist() {
        createdDate = new Date();
        updatedDate = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = new Date();
    }


}
