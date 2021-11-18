package com.logistics.supply.repository;

import com.logistics.supply.dto.RequestQuotationPair;
import com.logistics.supply.model.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Integer> {

  List<Quotation> findBySupplierId(int supplierId);

  Quotation findByRequestDocumentId(int requestDocumentId);

  @Query(
      value =
          "SELECT * from quotation q where q.request_document_id is NULL ORDER by q.created_at DESC",
      nativeQuery = true)
  List<Quotation> findQuotationWithoutDocument();

  @Query(
      value =
          "SELECT q.id as quotationId, riq.request_item_id as requestItemId from quotation q join request_item_quotations riq "
              + "on q.id = riq.quotation_id where riq.request_item_id in "
              + "(SELECT ri.id from request_item ri where ri.supplied_by is null) and q.request_document_id is NULL",
      nativeQuery = true)
  List<RequestQuotationPair> findQuotationRequestItemPairId();

  @Query(
      value =
          "SELECT * from quotation q where q.supplier_id =:supplierId and q.request_document_id is NULL ORDER by q.created_at DESC",
      nativeQuery = true)
  List<Quotation> findQuotationBySupplierId(@Param("supplierId") int supplierId);

  @Query(
      value =
          "select * from quotation q where q.expired = false and q.linked_to_lpo = false and q.id in (select riq.quotation_id from request_item_quotations riq)",
      nativeQuery = true)
  List<Quotation> findAllNonExpiredNotLinkedToLPO();

  @Transactional
  @Modifying
  @Query(value = "update quotation set linked_to_lpo = true where id =:id", nativeQuery = true)
  void updateLinkedToLPO(@Param("id") int id);


  @Query(value = "select * from quotation q where q.expired = false and q.linked_to_lpo = true", nativeQuery = true)
  List<Quotation> findByLinkedToLpoTrue();

  @Query(
      value =
          "select * from quotation q where q.expired = false and q.linked_to_lpo = false and q.id in (select riq.quotation_id from request_item_quotations riq where riq.request_item_id in (:rids))",
      nativeQuery = true)
  List<Quotation> findNonExpiredNotLinkedToLPO(@Param("rids") List<Integer> request_item_ids);
}
