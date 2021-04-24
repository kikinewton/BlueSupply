package com.logistics.supply.repository;

import com.logistics.supply.model.RequestItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface RequestItemRepository extends JpaRepository<RequestItem, Integer> {

  static final String GET_REQUEST_ITEMS_FOR_DEPARTMENT_FOR_HOD =
      "select\n"
          + "\t*\n"
          + "from\n"
          + "\trequest_item r\n"
          + "where\n"
          + "\tr.status = 'PENDING'\n"
          + "\tAND r.endorsement = 'PENDING'\n"
          + "\tAND r.employee_id in (\n"
          + "\tselect\n"
          + "\t\te.id\n"
          + "\tfrom\n"
          + "\t\temployee e\n"
          + "\twhere\n"
          + "\t\te.department_id =:departmentId);";

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
  List<RequestItem> getByEmployeeId(@Param("employeeId") Integer employeeId);

  @Query(
      value = "Select * from request_item r where r.supplier_id =:supplierId",
      nativeQuery = true)
  List<RequestItem> getBySupplier(@Param("supplierId") Integer supplierId);

  @Query(
      value =
          "SELECT * FROM request_item r where r.endorsement = 'ENDORSED' and r.status = 'PENDING' and r.supplied_by is null",
      nativeQuery = true)
  List<RequestItem> getEndorsedRequestItems();

  @Query(
      value =
          "SELECT * FROM request_item r where r.endorsement = 'ENDORSED' and r.status = 'PENDING'",
      nativeQuery = true)
  List<RequestItem> getRequestItemsForGeneralManager();

  @Query(
      value =
          "SELECT * FROM request_item r where r.approval = 'APPROVED' and r.status = 'PROCESSED'",
      nativeQuery = true)
  Collection<RequestItem> getApprovedRequestItems();

  @Query(
      value = "Select * from request_item r where r.employee_id =:employeeId order by r.id desc",
      nativeQuery = true)
  Collection<RequestItem> getEmployeeRequest(@Param("employeeId") Integer employeeId);

  @Query(value = GET_REQUEST_ITEMS_FOR_DEPARTMENT_FOR_HOD, nativeQuery = true)
  List<RequestItem> getRequestItemForHOD(@Param("departmentId") int departmentId);

  @Query(
      value =
          "SELECT count(ri.id) as num_of_req  from request_item ri where MONTH (ri.created_date) =  MONTH (CURDATE())",
      nativeQuery = true)
  Integer totalRequestPerCurrentMonth();

  @Query(
      value =
          "SELECT * FROM request_item r where r.endorsement = 'ENDORSED' and r.status = 'PENDING' and r.id in (SELECT ris.request_id from request_item_suppliers ris)",
      nativeQuery = true)
  List<RequestItem> getEndorsedRequestItemsWithSuppliersAssigned();

  @Query(
      value =
          "SELECT * from request_item ri where ri.id in (SELECT request_id from request_item_suppliers ris where supplier_id =:supplierId ) and ri.supplied_by is null",
      nativeQuery = true)
  List<RequestItem> getRequestItemsBySupplierId(@Param("supplierId") int supplierId);

  //  @Query("UPDATE request_item SET supplied_by=:supplierId WHERE id =:requestItemId")
  //  @Modifying
  //  @Transactional
  //  public void assignFinalSupplier(
  //      @Param("supplierId") int supplierId, @Param("requestItemId") int requestItemId);

}
