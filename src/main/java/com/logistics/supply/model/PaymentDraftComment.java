package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.AbstractAuditable;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@ToString
@Entity
@NoArgsConstructor
@JsonIgnoreProperties(value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class PaymentDraftComment extends AbstractAuditable<Employee, Integer> {

    @Column(length = 1000)
    String description;

    boolean read;

    @ManyToOne
    @JoinColumn(name = "payment_draft_id")
    PaymentDraft paymentDraft;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;

}
