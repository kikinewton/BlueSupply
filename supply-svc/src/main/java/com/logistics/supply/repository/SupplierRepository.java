package com.logistics.supply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.Supplier;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository
    extends JpaRepository<Supplier, Integer>, JpaSpecificationExecutor<Supplier> {

  Optional<Supplier> findByName(String name);

  Optional<Supplier> findByNameEqualsIgnoreCase(@NonNull String name);

  @Query(value = "select * from get_suppliers_for_rfq()", nativeQuery = true)
  List<Supplier> findSuppliersWithNonFinalRequestProcurement_dep();

  @Query(
          value =
                  "SELECT * from supplier s where s.id in "
                          + "( SELECT ris.supplier_id from request_item_suppliers ris join "
                          + "request_item ri on ris.request_id = ri.id and upper(ri.endorsement) = 'ENDORSED' and upper(ri.status) != 'PROCESSED')",
          nativeQuery = true)
  List<Supplier> findSuppliersWithNonFinalRequestProcurementO();

  @Query(
      value =
          "select * from supplier s where s.id in (select distinct(rfq.supplier_id) from request_for_quotation rfq where rfq.quotation_received is false)",
      nativeQuery = true)
  List<Supplier> findSuppliersWithNonFinalRequestProcurement();

  @Query(
      value =
          "SELECT * from supplier s where s.id in ( SELECT distinct(ris.supplier_id) from request_item_suppliers ris where"
              + " ris.request_id not in (SELECT ri.id from request_item ri where ri.supplied_by is null AND upper(ri.endorsement) = 'ENDORSED' "
              + " and upper(ri.status) = 'PENDING'))",
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
          "Select * from supplier s where s.registered is true and s.id in (Select srm.supplier_id from supplier_request_map srm where srm.document_attached is false and srm.request_item_id in (select id from request_item ri where ri.supplied_by is null))",
      nativeQuery = true)
  List<Supplier> findSupplierWithNoDocAttachedToUnProcessedRequestItems();

  @Query(
          value =
                  "Select * from supplier s where s.registered is not true and s.id in (Select srm.supplier_id from supplier_request_map srm where srm.document_attached is false and srm.request_item_id in (select id from request_item ri where ri.supplied_by is null))",
          nativeQuery = true)
  List<Supplier> findUnRegisteredSupplierWithNoDocAttachedFromSRM();

  @Query(
      value =
          "select s.* from supplier s where s.id in ( select distinct(supplier_id) from supplier_request_map srm where srm.document_attached is true)",
      nativeQuery = true)
  List<Supplier> findSuppliersWithQuotationAndDoc();

  @Query(
      value =
          "select s.* from supplier s where s.id in ( select distinct(supplier_id) from supplier_request_map srm where srm.document_attached is true) and s.id not in (select lpo.supplier_id from local_purchase_order lpo where is_approved is null and approved_by_id is null)",
      nativeQuery = true)
  List<Supplier> findSuppliersWithQuotationsWithoutLPO();

  @Query(value = "Select * from supplier where registered is not true", nativeQuery = true)
  List<Supplier> findByRegisteredNotTrue();
}
