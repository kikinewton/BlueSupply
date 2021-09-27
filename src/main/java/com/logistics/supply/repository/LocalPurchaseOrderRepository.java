package com.logistics.supply.repository;

import com.logistics.supply.model.LocalPurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  @Query(
      value =
          "SELECT * from local_purchase_order lpo where lpo.id in "
              + "(SELECT lpori.local_purchase_order_id from local_purchase_order_request_items lpori "
              + "where lpori.request_items_id =:requestItemId)",
      nativeQuery = true)
  LocalPurchaseOrder findLpoByRequestItem(@Param("requestItemId") int requestItemId);
}
