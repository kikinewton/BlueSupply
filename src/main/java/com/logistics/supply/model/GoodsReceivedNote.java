package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class GoodsReceivedNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String invoiceNo;

    @OneToOne
    private ProcuredItem procuredItem;

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
