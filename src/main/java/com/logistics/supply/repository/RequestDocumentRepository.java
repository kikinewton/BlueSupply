package com.logistics.supply.repository;

import com.logistics.supply.model.RequestDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestDocumentRepository extends JpaRepository<RequestDocument, Integer> {

  @Query(
      value =
          "Select * from request_document rd where rd.employee_id :=employeeId and rd.document_type :=documentType",
      nativeQuery = true)
  RequestDocument findDocumentByEmployeeId(
      @Param("employeeId") int employeeId, @Param("documentType") String docType);

  RequestDocument findByFileName(String fileName);
}
