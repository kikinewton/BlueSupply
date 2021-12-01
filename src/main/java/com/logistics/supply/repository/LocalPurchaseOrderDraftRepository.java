package com.logistics.supply.repository;

import com.logistics.supply.model.LocalPurchaseOrderDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalPurchaseOrderDraftRepository
    extends JpaRepository<LocalPurchaseOrderDraft, Integer> {

  List<LocalPurchaseOrderDraft> findBySupplierId(int supplierId);

  @Query(
      value =
          "SELECT * from local_purchase_order_draft lpod where lpod.id not in (select lpo.local_purchase_order_draft_id from local_purchase_order lpo) and lpod.delivery_date >= current_date",
      nativeQuery = true)
  List<LocalPurchaseOrderDraft> findDraftAwaitingApproval();

//  @Query(
//      value =
//          "SELECT * from local_purchase_order_draft lpod where lpod.id in "
//              + "(SELECT grn.local_purchase_order_draft_id from goods_received_note grn) order by lpod.id DESC",
//      nativeQuery = true)
//  List<LocalPurchaseOrderDraft> findLPOLinkedToGRN();

  @Query(
      value =
          "SELECT * from local_purchase_order_draft lpod where lpod.id in "
              + "(SELECT lpodri.local_purchase_order_id from local_purchase_order_draft_request_items lpodri "
              + "where lpodri.request_items_id =:requestItemId)",
      nativeQuery = true)
  LocalPurchaseOrderDraft findLpoByRequestItem(@Param("requestItemId") int requestItemId);
}
