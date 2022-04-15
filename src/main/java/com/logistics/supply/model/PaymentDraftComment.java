package com.logistics.supply.model;

import com.logistics.supply.enums.RequestProcess;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@ToString
@Entity
@NoArgsConstructor
public class PaymentDraftComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(length = 1000)
    String description;

    boolean read;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    RequestProcess processWithComment;

    @ManyToOne
    @JoinColumn(name = "payment_draft_id")
    PaymentDraft paymentDraft;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;

    @CreationTimestamp
    Date createdDate;

    @UpdateTimestamp
    Date updatedDate;

}
