package com.logistics.supply.repository;

import com.logistics.supply.model.LocalPurchaseOrderDraft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalPurchaseOrderDraftRepository
    extends JpaRepository<LocalPurchaseOrderDraft, Integer> {

  List<LocalPurchaseOrderDraft> findBySupplierId(int supplierId);

  Page<LocalPurchaseOrderDraft> findBySupplierId(int supplierId, Pageable pageable);

  @Query(
      value =
          "SELECT * from local_purchase_order_draft lpod where lpod.id not in (select lpo.local_purchase_order_draft_id from local_purchase_order lpo)",
      nativeQuery = true)
  Page<LocalPurchaseOrderDraft> findDraftAwaitingApproval(Pageable pageable);

  @Query(
          value =
                  "SELECT * from local_purchase_order_draft lpod where lpod.id not in (select lpo.local_purchase_order_draft_id from local_purchase_order lpo)",
          nativeQuery = true)
  List<LocalPurchaseOrderDraft> findDraftAwaitingApproval();

  @Query(
      value =
          "SELECT * from local_purchase_order_draft lpod where lpod.id in "
              + "(SELECT lpodri.local_purchase_order_id from local_purchase_order_draft_request_items lpodri "
              + "where lpodri.request_items_id =:requestItemId)",
      nativeQuery = true)
  LocalPurchaseOrderDraft findLpoByRequestItem(@Param("requestItemId") int requestItemId);

  @Query(
          value = """
                  SELECT lpod.*
                  FROM local_purchase_order_draft AS lpod
                  JOIN quotation AS q ON lpod.quotation_id = q.id
                  LEFT JOIN local_purchase_order AS lpo ON lpod.id = lpo.local_purchase_order_draft_id
                  WHERE q.hod_review = false
                    AND lpo.local_purchase_order_draft_id IS NULL
                    AND lpod.department_id = :departmentId
                  """,
          nativeQuery = true)
  Page<LocalPurchaseOrderDraft> findDraftAwaitingApprovalByHod(int departmentId, Pageable pageable);
}
