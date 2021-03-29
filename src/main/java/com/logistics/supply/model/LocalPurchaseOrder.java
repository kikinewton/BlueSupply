package com.logistics.supply.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class LocalPurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String comment;

    @ManyToOne(fetch = FetchType.EAGER)
    private Employee procurementOfficer;

//    private RequestItem requestItem;

    @CreationTimestamp
    private Date createdDate;

    @UpdateTimestamp
    private Date updateDate;
}
