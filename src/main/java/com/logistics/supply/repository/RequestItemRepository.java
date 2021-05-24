package com.logistics.supply.repository;

import com.logistics.supply.dto.CostOfGoodsPerDepartmentPerMonth;
import com.logistics.supply.dto.ProcuredItemDto;
import com.logistics.supply.dto.RequestPerCategory;
import com.logistics.supply.dto.RequestPerUserDepartment;
import com.logistics.supply.model.RequestItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
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

  @Query(
      value = "UPDATE request_item SET supplied_by=:supplierId WHERE id =:requestItemId",
      nativeQuery = true)
  @Modifying
  @Transactional
  public void assignFinalSupplier(
      @Param("supplierId") int supplierId, @Param("requestItemId") int requestItemId);

  @Query(
      value =
          "select"
              + "    `ri`.`id` AS `id`,\n"
              + "    `ri`.`name` AS `name`,\n"
              + "    `ri`.`reason` AS `reason`,\n"
              + "    `ri`.`purpose` AS `purpose`,\n"
              + "    `ri`.`quantity` AS `quantity`,\n"
              + "    `ri`.`total_price` AS `total_price`,\n"
              + "    (\n"
              + "    select\n"
              + "        `d`.`name`\n"
              + "    from\n"
              + "        `department` `d`\n"
              + "    where\n"
              + "        (`d`.`id` = `ri`.`user_department`)) AS `user_department`,\n"
              + "    (\n"
              + "    select\n"
              + "        `rc`.`name`\n"
              + "    from\n"
              + "        `request_category` `rc`\n"
              + "    where\n"
              + "        (`rc`.`id` = `ri`.`request_category`)) AS `category`,\n"
              + "    (\n"
              + "    select\n"
              + "        `s`.`name`\n"
              + "    from\n"
              + "        `supplier` `s`\n"
              + "    where\n"
              + "        (`s`.`id` = `ri`.`supplied_by`)) AS `supplied_by`\n"
              + "from\n"
              + "    `request_item` `ri`\n"
              + "where\n"
              + "    (`ri`.`id` in (\n"
              + "    select\n"
              + "        `lpori`.`request_items_id`\n"
              + "    from\n"
              + "        `local_purchase_order_request_items` `lpori`)"
              + "    and `ri`.`created_date` BETWEEN CAST(:startDate AS DATE) and CAST(:endDate AS DATE))",
      nativeQuery = true)
  List<Object[]> getProcuredItems(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query(
      value =
          "SELECT ("
              + "SELECT name from department d where d.id = ri.user_department) as userDepartment, "
              + "COUNT(ri.user_department) as numOfRequest from request_item ri where ri.approval = 'APPROVED' "
              + "and DATE(ri.created_date) = CURRENT_DATE() group by user_department",
      nativeQuery = true)
  List<RequestPerUserDepartment> findApprovedRequestPerUserDepartmentToday();

  @Query(
      value =
          "SELECT ("
              + "SELECT name from request_category rc  where rc.id = ri.request_category) as requestCategory , "
              + "COUNT(ri.request_category) as numOfRequest from request_item ri where ri.approval = 'APPROVED' "
              + "and DATE(ri.created_date) = CURRENT_DATE() group by request_category",
      nativeQuery = true)
  List<RequestPerCategory> findApprovedRequestPerCategory();

  @Query(
      value =
          "select\n"
              + "\t(\n"
              + "\tselect\n"
              + "\t\td.name\n"
              + "\tfrom\n"
              + "\t\tdepartment d\n"
              + "\twhere\n"
              + "\t\t(d.id = ri.user_department)) AS userDepartment,\n"
              + "\tcoalesce(SUM(ri.total_price), 0) AS totalPrice\n"
              + "from\n"
              + "\trequest_item ri\n"
              + "where\n"
              + "\tri.id in (\n"
              + "\tselect\n"
              + "\t\tlpori.request_items_id\n"
              + "\tfrom\n"
              + "\t\tlocal_purchase_order_request_items lpori)\n"
              + "\tand (month(ri.created_date) = month(curdate()))\n"
              + "\tand ri.approval = 'APPROVED'\n"
              + "GROUP BY\n"
              + "\tuser_department",
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
          "SELECT DISTINCT(id) from request_item ri join request_item_suppliers ris "
              + "on ri.id = ris.request_id where ris.supplier_id =:supplierId",
      nativeQuery = true)
  List<Integer> findRequestItemsBySupplierIdNotFinal(@Param("supplierId") int supplierId);
}
