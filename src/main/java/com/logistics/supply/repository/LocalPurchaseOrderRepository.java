package com.logistics.supply.repository;

import com.logistics.supply.model.LocalPurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalPurchaseOrderRepository extends JpaRepository<LocalPurchaseOrder, Integer> {

  List<LocalPurchaseOrder> findBySupplierId(int supplierId);

  @Query(
      value =
          "SELECT * from local_purchase_order lpo where lpo.id not in "
              + "(SELECT grn.local_purchase_order_id from goods_received_note grn) order by lpo.id DESC",
      nativeQuery = true)
  List<LocalPurchaseOrder> findLPOUnattachedToGRN();

  @Query(
          value =
                  "SELECT count(id) from local_purchase_order lpo where lpo.id not in "
                          + "(SELECT grn.local_purchase_order_id from goods_received_note grn)",
          nativeQuery = true)
  int countLPOUnattachedToGRN();

  @Query(
      value =
          "SELECT * from local_purchase_order lpo where lpo.department_id = :departmentId and lpo.id not in "
              + "(SELECT grn.local_purchase_order_id from goods_received_note grn) order by lpo.id DESC",
      nativeQuery = true)
  List<LocalPurchaseOrder> findLPOUnattachedToGRNByDepartment(
      @Param("departmentId") int departmentId);

  @Query(
      value =
          "SELECT * from local_purchase_order lpo where lpo.id in "
              + "(SELECT grn.local_purchase_order_id from goods_received_note grn) order by lpo.id DESC",
      nativeQuery = true)
  List<LocalPurchaseOrder> findLPOLinkedToGRN();

  @Query(
      value =
          "SELECT * from local_purchase_order lpo where lpo.id in "
              + "(SELECT lpori.local_purchase_order_id from local_purchase_order_request_items lpori "
              + "where lpori.request_items_id =:requestItemId)",
      nativeQuery = true)
  Optional<LocalPurchaseOrder> findLpoByRequestItem(@Param("requestItemId") int requestItemId);

  @Query(
      value =
          "SELECT case when count(lpo.id) > 0 then true else false end from local_purchase_order lpo where lpo.id in "
              + "(SELECT lpori.local_purchase_order_id from local_purchase_order_request_items lpori "
              + "where lpori.request_items_id =:requestItemId)",
      nativeQuery = true)
  Boolean lpoExistByRequestItem(@Param("requestItemId") int requestItemId);

  Optional<LocalPurchaseOrder> findByLpoRef(String lpoRef);
}
