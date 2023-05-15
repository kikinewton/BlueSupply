package com.logistics.supply.model;

import com.logistics.supply.event.listener.SupplierRequestListener;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@EntityListeners(SupplierRequestListener.class)
public class SupplierRequestMap {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id", referencedColumnName = "id")
  private Supplier supplier;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "request_item_id", referencedColumnName = "id")
  private RequestItem requestItem;

  private boolean document_attached;

  @CreationTimestamp private Date createdDate;

  @UpdateTimestamp private Date updatedDate;

  public SupplierRequestMap(Supplier supplier, RequestItem requestItem) {
    this.supplier = supplier;
    this.requestItem = requestItem;
  }
}
