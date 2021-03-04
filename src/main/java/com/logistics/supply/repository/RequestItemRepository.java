package com.logistics.supply.repository;

import com.logistics.supply.model.RequestItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RequestItemRepository extends JpaRepository<RequestItem, Integer> {

  Page<RequestItem> findAll(Pageable pageable);

  @Query(
      value = "Select * from request_item r where Date(r.request_date) >=:fromDate",
      nativeQuery = true)
  List<RequestItem> getRequestBetweenDateAndNow(@Param("fromDate") String fromDate);

  @Query(
      value =
          "Select * from request_item r where r.approval=:approvalStatus and Date(r.request_date) >=:fromDate",
      nativeQuery = true)
  List<RequestItem> getByApprovalStatus(
      @Param("approvalStatus") String approvalStatus, @Param("fromDate") String fromDate);

  @Query(
      value =
          "Select * from request_item r where r.status=:requestStatus and Date(r.request_date) >=:fromDate",
      nativeQuery = true)
  List<RequestItem> getByStatus(
      @Param("requestStatus") String requestStatus, @Param("fromDate") String fromDate);

  @Query(
      value =
          "Select * from request_item r where r.reason =:requestReason and Date(r.request_date) >=:fromDate",
      nativeQuery = true)
  List<RequestItem> getByReason(
      @Param("requestReason") String requestReason, @Param("fromDate") String fromDate);

  @Query(value = "Select * from request_item r where r.employee_id=:employeeId", nativeQuery = true)
  Optional<RequestItem> getByEmployeeId(@Param("employeeId") String employeeId);

  @Query(
      value = "Select * from request_item r where r.supplier_id =:supplierId",
      nativeQuery = true)
  Optional<RequestItem> getBySupplier(@Param("supplierId") String supplierId);

  @Query(
      value =
          "SELECT * FROM request_item r where r.endorsement = 'ENDORSED' and r.status = 'PENDING'",
      nativeQuery = true)
  List<RequestItem> getEndorsedRequestItems();

  @Query(
      value =
          "SELECT * FROM request_item r where r.approval = 'APPROVED' and r.status = 'PROCESSED'",
      nativeQuery = true)
  Collection<RequestItem> getApprovedRequestItems();
}
