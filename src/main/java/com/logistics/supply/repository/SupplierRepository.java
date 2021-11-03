package com.logistics.supply.repository;

import com.logistics.supply.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer>, JpaSpecificationExecutor<Supplier> {

  Optional<Supplier> findByName(String name);


  @Query(
      value =
          "SELECT * from supplier s where s.id in "
              + "( SELECT ris.supplier_id from request_item_suppliers ris join "
              + "request_item ri on ris.request_id = ri.id and upper(ri.endorsement) = 'ENDORSED' and upper(ri.status) != 'PROCESSED')",
      nativeQuery = true)
  List<Supplier> findSuppliersWithNonFinalRequestProcurement();

  @Query(
      value =
          "SELECT * from supplier s where s.id in ( SELECT distinct(ris.supplier_id) from request_item_suppliers ris where"
              + " ris.request_id in (SELECT ri.id from request_item ri where ri.supplied_by is null AND upper(ri.endorsement) = 'ENDORSED' "
              + "and upper(ri.status) = 'PENDING'))",
      nativeQuery = true)
  List<Supplier> findSuppliersWithoutDocumentInQuotation();

  @Query(
      value =
          "SELECT * from supplier s where s.id in ( SELECT distinct(ris.supplier_id) from request_item_suppliers ris "
              + "where ris.request_id in (SELECT ri.id from request_item ri where ri.supplied_by is null AND upper(ri.endorsement) = 'ENDORSED'"
              + " and upper(ri.status) = 'PENDING' and ri.id in (Select riq.request_item_id from request_item_quotations riq)))",
      nativeQuery = true)
  List<Supplier> findSuppliersWithQuotation();

  @Query(
      value =
          "Select * from supplier s where s.id in (Select srm.supplier_id from supplier_request_map srm where srm.document_attached is false)",
      nativeQuery = true)
  List<Supplier> findSupplierWithNoDocAttachedFromSRM();
}
