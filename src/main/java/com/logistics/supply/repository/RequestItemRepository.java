package com.logistics.supply.repository;

import com.logistics.supply.dto.CostOfGoodsPerDepartmentPerMonth;
import com.logistics.supply.dto.RequestPerCategory;
import com.logistics.supply.dto.RequestPerUserDepartment;
import com.logistics.supply.dto.SpendAnalysisDTO;
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
      "select * from request_item r where r.status = 'PENDING' AND r.endorsement = 'PENDING' AND r.employee_id in ("
          + "select e.id from employee e where e.department_id =:departmentId)";

  Page<RequestItem> findAll(Pageable pageable);

  @Query(
      value = "Select * from request_item r where Date(r.request_date) >=:fromDate",
      nativeQuery = true)
  List<RequestItem> getRequestBetweenDateAndNow(@Param("fromDate") String fromDate);

  @Query(
      value =
          "Select * from request_item r where r.approval = 'APPROVED' and r.status = 'PROCESSED' and r.id =:requestItemId",
      nativeQuery = true)
  Optional<RequestItem> findApprovedRequestById(@Param("requestItemId") int requestItemId);

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
  List<RequestItem> getApprovedRequestItems();

  @Query(
      value = "Select * from request_item r where r.employee_id =:employeeId order by r.id desc",
      nativeQuery = true)
  Collection<RequestItem> getEmployeeRequest(@Param("employeeId") Integer employeeId);

  @Query(value = GET_REQUEST_ITEMS_FOR_DEPARTMENT_FOR_HOD, nativeQuery = true)
  List<RequestItem> getRequestItemForHOD(@Param("departmentId") int departmentId);

  @Query(
      value =
          "select * from request_item r where r.status = 'PENDING' "
              + "AND r.endorsement = 'ENDORSED' AND r.employee_id in "
              + "( select e.id from employee e where e.department_id =:departmentId)",
      nativeQuery = true)
  List<RequestItem> getDepartmentEndorsedRequestItemForHOD(@Param("departmentId") int departmentId);

  @Query(
      value =
          "SELECT count(ri.id) as num_of_req  from request_item ri where EXTRACT(MONTH FROM ri.created_date) =  EXTRACT(MONTH FROM CURRENT_DATE)",
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

  @Query(
      value = "UPDATE request_item SET supplied_by=:supplierId WHERE id =:requestItemId",
      nativeQuery = true)
  @Modifying
  @Transactional
  public void assignFinalSupplier(
      @Param("supplierId") int supplierId, @Param("requestItemId") int requestItemId);

  @Query(
      value =
          "SELECT\n"
              + "\tRI.ID AS ID,\n"
              + "\tRI.NAME AS NAME,\n"
              + "\tRI.REASON AS REASON,\n"
              + "\tRI.PURPOSE AS PURPOSE,\n"
              + "\tRI.QUANTITY AS QUANTITY,\n"
              + "\tRI.TOTAL_PRICE AS TOTAL_PRICE,\n"
              + "\t(\n"
              + "\tSELECT\n"
              + "\t\tD.NAME\n"
              + "\tFROM\n"
              + "\t\tDEPARTMENT D\n"
              + "\tWHERE\n"
              + "\t\t(D.ID = RI.USER_DEPARTMENT)) AS USER_DEPARTMENT,\n"
              + "\t(\n"
              + "\tSELECT\n"
              + "\t\tRC.NAME\n"
              + "\tFROM\n"
              + "\t\tREQUEST_CATEGORY RC\n"
              + "\tWHERE\n"
              + "\t\t(RC.ID = RI.REQUEST_CATEGORY)) AS CATEGORY,\n"
              + "\t(\n"
              + "\tSELECT\n"
              + "\t\tS.NAME\n"
              + "\tFROM\n"
              + "\t\tSUPPLIER S\n"
              + "\tWHERE\n"
              + "\t\t(S.ID = RI.SUPPLIED_BY)) AS SUPPLIED_BY\n"
              + "FROM\n"
              + "\tREQUEST_ITEM RI\n"
              + "WHERE\n"
              + "\t(RI.ID IN (\n"
              + "\tSELECT\n"
              + "\t\tLPORI.REQUEST_ITEMS_ID\n"
              + "\tFROM\n"
              + "\t\tLOCAL_PURCHASE_ORDER_REQUEST_ITEMS LPORI)\n"
              + "\tAND RI.CREATED_DATE BETWEEN CAST(:startDate AS DATE) AND CAST(:endDate AS DATE));",
      nativeQuery = true)
  List<Object[]> getProcuredItems(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query(
      value =
          "SELECT ("
              + "SELECT name from department d where d.id = ri.user_department) as userDepartment, "
              + "COUNT(ri.user_department) as numOfRequest from request_item ri where ri.approval = 'APPROVED' "
              + "and DATE(ri.created_date) = CURRENT_DATE group by user_department",
      nativeQuery = true)
  List<RequestPerUserDepartment> findApprovedRequestPerUserDepartmentToday();

  @Query(
      value =
          "SELECT ("
              + "SELECT name from request_category rc  where rc.id = ri.request_category) as requestCategory , "
              + "COUNT(ri.request_category) as numOfRequest from request_item ri where ri.approval = 'APPROVED' "
              + "and DATE(ri.created_date) = CURRENT_DATE group by request_category",
      nativeQuery = true)
  List<RequestPerCategory> findApprovedRequestPerCategory();

  @Query(
      value =
          "select (select d.name from department d where (d.id = ri.user_department)) AS userDepartment, "
              + "coalesce(SUM(ri.total_price), 0) AS totalPrice from request_item ri where ri.id in "
              + "(select lpori.request_items_id from local_purchase_order_request_items lpori) and "
              + "(EXTRACT(MONTH FROM ri.created_date) = EXTRACT(MONTH FROM CURRENT_DATE)) and "
              + "ri.approval = 'APPROVED' GROUP BY user_department",
      nativeQuery = true)
  List<CostOfGoodsPerDepartmentPerMonth> findCostOfGoodsPaidPerDepartmentPerMonth();

  @Query(
      value =
          "SELECT DISTINCT(ri.id) FROM request_item ri join request_item_quotations riq"
              + " on riq.request_item_id = ri.id where riq.quotation_id in "
              + "(SELECT q.id from quotation q where q.request_document_id is null and supplier_id is not null)",
      nativeQuery = true)
  List<Integer> findItemIdWithoutDocsInQuotation();

  @Query(
      value =
          "SELECT s.name , coalesce(SUM(grn.invoice_amount_payable), 0) as paymentAmount from goods_received_note grn"
              + " join supplier s on s.id = grn.supplier where grn.local_purchase_order_id in "
              + "( SELECT local_purchase_order_id from local_purchase_order_request_items lpori) "
              + "group by grn.supplier, s.name",
      nativeQuery = true)
  List<SpendAnalysisDTO> supplierSpendAnalysis();

  @Query(
      value =
          "SELECT distinct(ri.id) from request_item ri join request_item_quotations riq on ri.id = riq.request_item_id "
              + "join request_item_suppliers ris on ris.request_id = ri.id where ri.supplied_by is null and riq.quotation_id in "
              + "( SELECT q.id from quotation q where q.request_document_id is null) and ris.supplier_id =:supplierId",
      nativeQuery = true)
  List<Integer> findRequestItemsForSupplier(@Param("supplierId") int supplierId);
}
