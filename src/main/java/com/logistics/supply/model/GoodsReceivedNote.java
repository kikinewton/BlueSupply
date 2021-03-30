package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Data
public class GoodsReceivedNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    private String invoiceNo;

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "goodsReceivedNote")
    private Set<RequestItem> requestItem;

    @JsonIgnore
    @CreationTimestamp
    private Date createdDate;

    @JsonIgnore
    @UpdateTimestamp
    private Date updateDate;

    @ManyToOne
    @JoinColumn(name = "issued_by")
    private Employee issuedBy;



}
