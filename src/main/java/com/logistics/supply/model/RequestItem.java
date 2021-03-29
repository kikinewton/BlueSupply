package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.logistics.supply.enums.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Slf4j
@Data
public class RequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestReason reason;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    @PositiveOrZero
    private Integer quantity = 0;

    @Column
    @PositiveOrZero
    private BigDecimal unitPrice = BigDecimal.valueOf(0);

    @Column
    @PositiveOrZero
    private BigDecimal amount = BigDecimal.valueOf(0);

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="supplier_id")
    private Supplier supplier;

    @Column
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    @Column
    @Enumerated(EnumType.STRING)
    private RequestApproval approval = RequestApproval.PENDING;

    @JsonIgnore
    Date approvalDate;

    @JsonIgnore
    Date endorsementDate;

    @Column
    @Enumerated(EnumType.STRING)
    private EndorsementStatus endorsement = EndorsementStatus.PENDING;

    @Column
    private Date requestDate = new Date();

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="employee_id")
    private Employee employee;

    @OneToOne
    @JoinColumn(name = "user_department")
    private Department userDepartment;

    @Enumerated(EnumType.STRING)
    private RequestType requestType;


    @JsonIgnore
    Date createdDate;

    @JsonIgnore
    Date updatedDate;


//    @PrePersist
//    public void prePersist() {
//        createdDate = new Date();
//        updatedDate = new Date();
//        endorsementDate = new Date();
//    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = new Date();
        log.info("Attempting to update requestItem: " + id);
    }

    @PrePersist
    public void logNewRequestItemAttempt() {
        createdDate = new Date();
        updatedDate = new Date();
        endorsementDate = new Date();
        log.info("Attempting to add new request with name: " + name);
    }

    @PostPersist
    public void logNewRequestItemAdded() {
        log.info("Added requestItem '" + name + "' with ID: " + id);
    }

    @PreRemove
    public void logRequestItemRemovalAttempt() {
        log.info("Attempting to delete requestItem: " + id);
    }

    @PostRemove
    public void logRequestItemRemoval() {
        log.info("Deleted requestItem: " + id);
    }
//
//    @PreUpdate
//    public void logRequestItemUpdateAttempt() {
//        log.info("Attempting to update requestItem: " + id);
//    }

    @PostUpdate
    public void logRequestItemUpdate() {
        log.info("Updated requestItem: " + id);
    }




}
