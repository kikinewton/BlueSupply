package com.logistics.supply.repository;

import com.logistics.supply.dto.CostOfGoodsPerDepartmentPerMonth;
import com.logistics.supply.dto.RequestPerCategory;
import com.logistics.supply.dto.RequestPerUserDepartment;
import com.logistics.supply.dto.SpendAnalysisDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public interface RequestItemRepository
    extends JpaRepository<RequestItem, Integer>, JpaSpecificationExecutor<RequestItem> {

  static final String GET_REQUEST_ITEMS_FOR_DEPARTMENT_FOR_HOD =
      "select * from request_item r where deleted = false and upper(r.endorsement) = 'PENDING' and r.id not in ( select ris.request_id from request_item_suppliers ris) and r.employee_id in ( select e.id from employee e where e.department_id =:departmentId) or (r.user_department =:departmentId and upper(r.status) = 'PENDING' and upper(r.endorsement) = 'PENDING' and r.id not in ( select ris.request_id from request_item_suppliers ris))";

  Page<RequestItem> findAll(Pageable pageable);

  @Query(
      value = "Select * from request_item r where Date(r.request_date) >=:fromDate",
      nativeQuery = true)
  List<RequestItem> getRequestBetweenDateAndNow(@Param("fromDate") String fromDate);

  @Query(
      value =
          "Select * from request_item r where upper(r.approval) = 'APPROVED' and upper(r.status) = 'PROCESSED' and r.id =:requestItemId",
      nativeQuery = true)
  Optional<RequestItem> findApprovedRequestById(@Param("requestItemId") int requestItemId);

  @Query(
      value =
          "select ri.* from request_item ri join request_item_quotations riq on ri.id = riq.request_item_id where riq.quotation_id =:quotationId",
      nativeQuery = true)
  List<RequestItem> findByQuotationId(@Param("quotationId") int quotationId);

  @Query(
      value =
          "Select * from request_item r where upper(r.request_review) =:reviewStatus and upper(r.status) = 'PROCESSED' and upper(r.approval) = 'PENDING' and r.employee_id in "
              + "(select e.id from employee e where e.department_id =:departmentId)",
      nativeQuery = true)
  List<RequestItem> findByRequestReview(
      @Param("reviewStatus") String reviewStatus, @Param("departmentId") int departmentId);

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
          "SELECT * FROM request_item r where upper(r.endorsement) = 'ENDORSED' and upper(r.status) = 'PENDING' and r.supplied_by is null ",
      nativeQuery = true)
  List<RequestItem> getEndorsedRequestItems();

  @Query(
      value =
          "Select * from request_item r where upper(r.endorsement) = 'ENDORSED' and upper(r.approval) = 'PENDING' and upper(r.status) = 'PENDING' and r.id in (Select ris.request_id From request_item_suppliers ris)",
      nativeQuery = true)
  List<RequestItem> getEndorsedRequestItemsWithSuppliersLinked();



  @Query(
      value =
          "Select * from request_item r where upper(r.endorsement) = 'ENDORSED' and upper(r.approval) = 'PENDING' and upper(r.status) = 'PENDING' and r.id not in (Select ris.request_id From request_item_suppliers ris);",
      nativeQuery = true)
  List<RequestItem> getEndorsedRequestItemsWithoutSupplier();

  @Query(
      value =
          "SELECT * FROM request_item r where upper(r.endorsement) = 'ENDORSED' and upper(r.status) = 'PENDING'",
      nativeQuery = true)
  List<RequestItem> getRequestItemsForGeneralManager();

  @Query(
      value =
          "SELECT * FROM request_item r where upper(r.approval) = 'APPROVED' and upper(r.status) = 'PROCESSED'",
      nativeQuery = true)
  List<RequestItem> getApprovedRequestItems();

  @Query(
      value =
          "Select * from request_item r where r.employee_id =:employeeId order by r.priority_level desc, r.id desc",
      nativeQuery = true)
  Collection<RequestItem> getEmployeeRequest(@Param("employeeId") Integer employeeId);

  Page<RequestItem> findByEmployee(Employee employee, Pageable pageable);

  @Query(value = GET_REQUEST_ITEMS_FOR_DEPARTMENT_FOR_HOD, nativeQuery = true)
  List<RequestItem> getRequestItemForHOD(@Param("departmentId") int departmentId);

  @Query(
      value =
          "select * from request_item r where upper(r.status) = 'PENDING' "
              + "AND upper(r.endorsement) = 'ENDORSED' AND r.employee_id in "
              + "( select e.id from employee e where e.department_id =:departmentId)",
      nativeQuery = true)
  List<RequestItem> getDepartmentEndorsedRequestItemForHOD(@Param("departmentId") int departmentId);

  @Query(
      value =
          "SELECT count(ri.id) as num_of_req  from request_item ri where EXTRACT(MONTH FROM ri.created_date) =  EXTRACT(MONTH FROM CURRENT_DATE) and upper(ri.approval) = 'APPROVED'",
      nativeQuery = true)
  Integer totalRequestPerCurrentMonth();

  @Cacheable(value = "requestForMonth")
  @Query(
          value =
                  "SELECT * from request_item ri where EXTRACT(MONTH FROM ri.created_date) =  EXTRACT(MONTH FROM CURRENT_DATE) and upper(ri.approval) = 'APPROVED'",
          nativeQuery = true)
  List<RequestItem> requestForCurrentMonth();

  @Query(
      value =
          "SELECT * FROM request_item r where deleted = false and upper(r.endorsement) = 'ENDORSED' and upper(r.approval) = 'PENDING' or upper(r.status) = 'COMMENT' and upper(r.status) = 'PROCESSED' and upper(r.request_review) = 'HOD_REVIEW' and r.id in (SELECT ris.request_id from request_item_suppliers ris)",
      nativeQuery = true)
  List<RequestItem> getEndorsedRequestItemsWithSuppliersAssigned();

  @Query(
      value =
          "SELECT * from request_item ri where deleted = false and ri.id in (SELECT request_id from request_item_suppliers ris where supplier_id =:supplierId ) and ri.supplied_by is null",
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
              + "\tAND RI.CREATED_DATE BETWEEN CAST(:startDate AS DATE) AND CAST(:endDate AS DATE))",
      nativeQuery = true)
  List<Object[]> getProcuredItems(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query(
      value =
          "SELECT ("
              + "SELECT name from department d where d.id = ri.user_department) as userDepartment, "
              + "COUNT(ri.user_department) as numOfRequest from request_item ri where upper(ri.approval) = 'APPROVED' "
              + "and DATE(ri.created_date) BETWEEN CURRENT_DATE - 7 AND CURRENT_DATE group by user_department",
      nativeQuery = true)
  List<RequestPerUserDepartment> findApprovedRequestPerUserDepartmentToday();

  @Query(
      value =
          "SELECT ("
              + "SELECT name from request_category rc  where rc.id = ri.request_category) as requestCategory , "
              + "COUNT(ri.request_category) as numOfRequest from request_item ri where upper(ri.approval) = 'APPROVED' "
              + "and DATE(ri.created_date) BETWEEN CURRENT_DATE - 7 AND CURRENT_DATE group by request_category",
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
          "SELECT distinct(ri.id) from request_item ri join request_item_suppliers ris on ri.id = ris.request_id "
              + "where ri.supplied_by is null and deleted = false and upper(ri.endorsement) = 'ENDORSED' and upper(ri.status) = 'PENDING' and ris.supplier_id =:supplierId",
      nativeQuery = true)
  List<Integer> findUnprocessedRequestItemsForSupplier(@Param("supplierId") int supplierId);

  @Query(
      value =
          "SELECT distinct(ri.id) from request_item ri join request_item_suppliers ris on ri.id = ris.request_id "
              + "where ri.supplied_by is null and upper(ri.endorsement) = 'ENDORSED' and upper(ri.status) = 'PENDING' and ris.supplier_id =:supplierId",
      nativeQuery = true)
  List<Integer> findRequestItemsForSupplierWithoutQuotation(@Param("supplierId") int supplierId);

  /**
   * This query returns request items with no document attached for the provided supplier id
   *
   * @return
   */
  @Query(
      value =
          "with cte as ( select srm.* from supplier_request_map srm where srm.document_attached is false and supplier_id = :supplierId) select * from request_item ri where ri.id in ( select request_item_id from cte)",
      nativeQuery = true)
  Set<RequestItem> findRequestItemsWithNoDocumentAttachedForSupplier(
      @Param("supplierId") int supplierId);


  @Query(
      value =
          "select * from request_item ri where ri.id in (select riq.request_item_id from request_item_quotations riq where riq.quotation_id =:quotationId) and supplied_by is not null",
      nativeQuery = true)
  List<RequestItem> findRequestItemsWithFinalPriceByQuotationId(
      @Param("quotationId") int quotationId);

  @Query(
      value =
          "select * from request_item ri where ri.id in (select lpori.request_items_id from local_purchase_order_request_items lpori)",
      nativeQuery = true)
  List<RequestItem> findRequestItemsWithLpo();

  List<RequestItem> findBySuppliedByNotNull();

  @Query(
      value =
          "select * from request_item ri where deleted = false and ri.id in (select riq.request_item_id from request_item_quotations riq where riq.quotation_id =:quotationId)",
      nativeQuery = true)
  List<RequestItem> findRequestItemsUnderQuotation(@Param("quotationId") int quotationId);

  long countByEndorsement(EndorsementStatus status);
  long countByApproval(RequestApproval approval);
  long countByStatus(RequestStatus status);

  @Query(value = "select count(id) from request_item", nativeQuery = true)
  long countAll();

}
