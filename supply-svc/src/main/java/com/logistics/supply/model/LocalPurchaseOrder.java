package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@SQLDelete(sql = "UPDATE local_purchase_order SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")
@EntityListeners({AuditingEntityListener.class})
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(
        value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new", "createdDate"})
public class LocalPurchaseOrder extends AbstractAuditable<Employee, Integer> {

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private Employee approvedBy;

    private Boolean isApproved;

    @Size(min = 1)
    @OneToMany
    private Set<RequestItem> requestItems;

    @Column(nullable = false, updatable = false)
    private Integer supplierId;

    @OneToOne
    private Quotation quotation;

    private String lpoRef;

    private boolean deleted;

    @Future
    private Date deliveryDate;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    @JsonIgnore
    private Date updatedDate;

    @OneToOne
    private Department department;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_purchase_order_draft_id")
    private LocalPurchaseOrderDraft localPurchaseOrderDraft;

    @Transient
    private String departmentHod;

}
