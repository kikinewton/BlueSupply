package com.logistics.supply.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "procurement_officer")
    private Employee procurementOfficer;

    @OneToMany
    @JoinColumn(name = "requestItem")
    private Set<RequestItem> requestItem;

    @ManyToOne
    private Supplier supplier;

}
