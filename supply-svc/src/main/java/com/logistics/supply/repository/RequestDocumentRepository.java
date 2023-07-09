package com.logistics.supply.repository;

import com.logistics.supply.model.RequestDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestDocumentRepository extends JpaRepository<RequestDocument, Integer> {

  @Query(
      value =
          "Select * from request_document rd where rd.employee_id :=employeeId and rd.document_type :=documentType",
      nativeQuery = true)
  RequestDocument findDocumentByEmployeeId(
      @Param("employeeId") int employeeId, @Param("documentType") String docType);

  Optional<RequestDocument> findByFileName(String fileName);

  @Query(value = "select * from request_document rd where rd.id in ( select q.request_document_id from quotation q " +
          "where q.id in ( select riq.quotation_id from request_item_quotations riq " +
          "where riq.request_item_id =:requestItemId))", nativeQuery = true)
  List<RequestDocument> findQuotationsByRequestItem(@Param("requestItemId") int requestItemId);
}
