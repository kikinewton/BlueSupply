package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@ToString
@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Invoice extends AbstractAuditable<Employee, Integer> {

    public Invoice() {
        // TODO document why this constructor is empty
    }

    @Column(updatable = false, length = 30)
    private String invoiceNumber;

    @ManyToOne
    private Supplier supplier;

    @OneToOne
    private RequestDocument invoiceDocument;

}
