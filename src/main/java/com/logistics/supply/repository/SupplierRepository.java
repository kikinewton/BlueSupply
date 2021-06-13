package com.logistics.supply.repository;

import com.logistics.supply.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

  Optional<Supplier> findByName(String name);

  @Query(
      value =
          "SELECT * from supplier s where s.id in "
              + "( SELECT ris.supplier_id from request_item_suppliers ris join "
              + "request_item ri on ris.request_id = ri.id and ri.endorsement = 'ENDORSED')",
      nativeQuery = true)
  List<Supplier> findSuppliersWithNonFinalRequestProcurement();

  @Query(
      value =
          "SELECT * from supplier s where s.id in "
              + "( SELECT q.supplier_id from quotation q where q.id in "
              + "( SELECT q.id from quotation q join request_item_quotations riq on q.id = riq.quotation_id"
              + " where riq.request_item_id in ( SELECT ri.id from request_item ri where ri.supplied_by is null)"
              + " and q.request_document_id is NULL))",
      nativeQuery = true)
  List<Supplier> findSuppliersWithoutDocumentInQuotation();
}
