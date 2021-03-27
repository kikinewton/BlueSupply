package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class GoodsReceivedNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String invoiceNo;


    private RequestItem requestItem;

    @JsonIgnore
    @CreationTimestamp
    private Date createdDate;

    @JsonIgnore
    @UpdateTimestamp
    private Date updateDate;


    private Employee issuedBy;



}
