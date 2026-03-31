package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.event.listener.LpoDraftEventListener;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Entity
@Getter
@Setter
@SQLRestriction("deleted=false")
@SQLDelete(sql = "UPDATE local_purchase_order_draft SET deleted = true WHERE id = ?")
@EntityListeners({AuditingEntityListener.class, LpoDraftEventListener.class})
@JsonIgnoreProperties(
        value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new", "createdDate"})
public class LocalPurchaseOrderDraft extends AbstractAuditable<Employee, Integer> {

    @Size(min = 1)
    @OneToMany(fetch = FetchType.EAGER)
    private Set<RequestItem> requestItems;

    @Column(nullable = false, updatable = false)
    private Integer supplierId;

    @OneToOne
    private Quotation quotation;

    @Future
    private Date deliveryDate;

    @CreationTimestamp
    private Date createdAt;

    private boolean deleted;

    @UpdateTimestamp
    @JsonIgnore
    private Date updatedDate;

    @OneToOne
    private Department department;

    @PostUpdate
    public void logAfterUpdate() {
        updatedDate = new Date();
    }

    @PrePersist
    private void setDepartment() {
        Optional<Department> departmentOptional = requestItems.stream()
                .map(RequestItem::getUserDepartment).findAny();

        if (departmentOptional.isPresent()) department = departmentOptional.get();
    }
}
