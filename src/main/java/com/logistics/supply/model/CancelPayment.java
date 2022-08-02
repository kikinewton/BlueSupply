package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@JsonIgnoreProperties(
        value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class CancelPayment extends AbstractAuditable<Employee, Integer> {
    private String comment;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    public CancelPayment(String comment, Payment payment, Employee employee) {
        this.comment = comment;
        this.payment = payment;
        this.setCreatedBy(employee);
    }
}
