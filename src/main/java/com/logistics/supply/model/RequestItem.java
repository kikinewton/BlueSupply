package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    @PositiveOrZero
    private Integer quantity = 0;

    @Column
    @PositiveOrZero
    private Float unitPrice = 0f;

    @Column
    @PositiveOrZero
    private Float amount = 0f;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="supplier_id")
    private Supplier supplier;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private RequestApproval approval = RequestApproval.PENDING;

    @JsonIgnore
    Date approvalDate;

    @JsonIgnore
    Date endorsementDate;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private EndorsementStatus endorsement = EndorsementStatus.PENDING;

    @Column
    private Date requestDate = new Date();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="employee_id")
    private Employee employee;


    @JsonIgnore
    Date createdDate;

    @JsonIgnore
    Date updatedDate;


    @PrePersist
    public void prePersist() {
        createdDate = new Date();
        updatedDate = new Date();
        endorsementDate = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = new Date();


    }


}
