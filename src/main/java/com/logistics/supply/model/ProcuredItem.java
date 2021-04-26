package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Data
public class ProcuredItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
//    @JoinTable(
//            name = "procured_item_request_item",
//            joinColumns = @JoinColumn(name = "procured_item_id", nullable = false),
//            inverseJoinColumns = @JoinColumn(name = "request_item_id", nullable = false))
//    private Set<RequestItem> requestItems;

    @OneToOne(fetch = FetchType.EAGER)
    LocalPurchaseOrder localPurchaseOrder;

    @JsonIgnore
    @CreationTimestamp
    Date createdDate;

    @JsonIgnore
    Date updatedDate;

    @PostUpdate
    public void logAfterUpdate() {
        updatedDate = new Date();
    }
}
