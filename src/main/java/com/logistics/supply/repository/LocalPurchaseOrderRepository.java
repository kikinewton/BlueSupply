package com.logistics.supply.repository;

import com.logistics.supply.model.LocalPurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalPurchaseOrderRepository extends JpaRepository<LocalPurchaseOrder, Integer> {

  List<LocalPurchaseOrder> findBySupplierId(int supplierId);

  @Query(
      value =
          "SELECT * from local_purchase_order lpo where lpo.id not in "
              + "(SELECT grn.local_purchase_order_id from goods_received_note grn) order by lpo.created_date DESC",
      nativeQuery = true)
  List<LocalPurchaseOrder> findLPOUnattachedToGRN();

}
