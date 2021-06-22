package com.logistics.supply.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class SupplierRequestMap {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id", referencedColumnName = "id")
  Supplier supplier;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "request_item_id", referencedColumnName = "id")
  RequestItem requestItem;

  boolean document_attached;

  @CreationTimestamp Date createdDate;

  @UpdateTimestamp Date updatedDate;

  public SupplierRequestMap(Supplier supplier, RequestItem requestItem) {
    this.supplier = supplier;
    this.requestItem = requestItem;
  }
}
