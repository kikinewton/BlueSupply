package com.logistics.supply.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@SQLDelete(sql = "UPDATE quotation SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    private Supplier supplier;

    @Column(length = 30)
    private String quotationRef;

    private boolean linkedToLpo;

    private boolean expired;

    private boolean deleted;

    @Column(name = "hod_review")
    private boolean hodReview;

    private boolean auditorReview;

    @OneToOne
    private RequestDocument requestDocument;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;

    @ManyToOne
    @JoinColumn(name = "auditor_id")
    private Employee auditor;

    @ManyToOne
    @JoinColumn(name = "hod_id")
    private Employee hod;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    private Date hodReviewDate;

    private Date auditorReviewDate;
}
