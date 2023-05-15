package com.logistics.supply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.supply.model.SupplierRequestMap;

@Repository
public interface SupplierRequestMapRepository extends JpaRepository<SupplierRequestMap, Integer> {

  @Query(
      value =
          "UPDATE supplier_request_map  SET document_attached=true WHERE request_item_id =:requestItemId and supplier_id =:supplierId",
      nativeQuery = true)
  @Modifying
  @Transactional
  public void updateDocumentStatus(
      @Param("requestItemId") int requestItemId, @Param("supplierId") int supplierId);
}
