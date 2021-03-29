package com.logistics.supply.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Supplier supplier;

    @OneToMany
    @JoinColumn(name = "payment_request_item")
    private Set<RequestItem> requestItem;

    @OneToOne
    private Invoice invoice;

    @OneToOne
    private LocalPurchaseOrder localPurchaseOrder;

    @OneToOne
    private GoodsReceivedNote goodsReceivedNote;

    @OneToOne
    private Employee accountOfficer;
}
