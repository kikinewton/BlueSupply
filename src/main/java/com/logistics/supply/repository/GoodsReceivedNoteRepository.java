package com.logistics.supply.repository;

import com.logistics.supply.interfaces.projections.GRNView;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.LocalPurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceivedNoteRepository extends JpaRepository<GoodsReceivedNote, Long> {
  List<GoodsReceivedNote> findBySupplier(int supplierId);

  List<GoodsReceivedNote> findByApprovedByHodFalse();

  @Query(value = "SELECT * from local_purchase_order lpo where lpo.department_id =:departmentId and lpo.id not in (SELECT grn.local_purchase_order_id from goods_received_note grn) order by lpo.id desc", nativeQuery = true)
  List<GoodsReceivedNote> findByDepartmentAndApprovedByHodFalse(@Param("departmentId") int departmentId);

  Optional<GoodsReceivedNote> findByLocalPurchaseOrder(LocalPurchaseOrder localPurchaseOrder);

  @Query(
      value = "SELECT * from goods_received_note grn where grn.invoice_id =:invoiceId",
      nativeQuery = true)
  Optional<GoodsReceivedNote> findByInvoiceId(@Param("invoiceId") int invoiceId);

  @Query(
      value =
          "SELECT count(*) from goods_received_note grn where DATE(grn.created_date) = CURRENT_DATE",
      nativeQuery = true)
  int findCountOfGRNForToday();

  @Query(
      value = "SELECT * from goods_received_note grn where DATE(grn.created_date) = CURRENT_DATE",
      nativeQuery = true)
  List<GoodsReceivedNote> findGRNIssuedToday();

  @Query(
      value =
          "select * from goods_received_note grn where grn.id not in (SELECT p.goods_received_note_id from payment p where p.deleted = false) and grn.payment_date <= current_date + interval '7 day' union (select * from goods_received_note grn where grn.id in (SELECT p.goods_received_note_id from payment p where UPPER(p.payment_status) != UPPER('COMPLETED') and p.deleted = true))",
      nativeQuery = true)
  List<GoodsReceivedNote> findPaymentDueInOneWeek();

  @Query(
      value =
          "with c1 as ( select * from goods_received_note grn where grn.id not in ( select p.goods_received_note_id from payment p where p.deleted = false) and grn.payment_date <= current_date + interval '7 day' union ( select * from goods_received_note grn where grn.id in ( select p.goods_received_note_id from payment p where UPPER(p.payment_status) != UPPER('COMPLETED') and p.deleted = true))) select count(*) from c1",
      nativeQuery = true)
  int findNumberOfPaymentDueInOneWeek();

  @Query(
      value =
          "select * from grn_report g where g.date_received BETWEEN CAST(:startDate AS DATE) and CAST(:endDate AS DATE) order by g.id desc",
      nativeQuery = true)
  List<Object[]> getGoodsReceivedNoteReport(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query(
      value =
          "SELECT * from goods_received_note grn where grn.id not in (SELECT p.goods_received_note_id from payment p where p.deleted = false) and grn.payment_date is not null"
              + " UNION SELECT * from goods_received_note grn where grn.id in (SELECT p.goods_received_note_id from payment p where UPPER(p.payment_status) = 'PARTIAL' or p.deleted = true)",
      nativeQuery = true)
  List<GoodsReceivedNote> grnWithoutCompletePayment();

  List<GoodsReceivedNote> findByPaymentDateIsNullAndApprovedByHodTrue();

  @Query(
      value =
          "SELECT grn.id, (SELECT s.name  FROM supplier s WHERE s.id = grn.supplier) AS supplier, grn.grn_ref as grnRef, (SELECT e.full_name  FROM employee e WHERE e.id = created_by_id) AS createdBy, grn.payment_date as paymentDate, grn.created_date as createdDate, grn.date_of_approval_by_hod as dateOfApprovalHod FROM goods_received_note grn WHERE grn.payment_date < current_date AND grn.id NOT IN (SELECT p.goods_received_note_id FROM payment p WHERE p.deleted = false) "
              + "UNION SELECT grn.id, (SELECT s.name  FROM supplier s WHERE s.id = grn.supplier) AS supplier, grn.grn_ref as grnRef, (SELECT e.full_name  FROM employee e WHERE e.id = created_by_id) AS createdBy, grn.payment_date as paymentDate, grn.created_date as createdDate, grn.date_of_approval_by_hod as dateOfApprovalHod FROM goods_received_note grn WHERE grn.payment_date < current_date AND grn.id IN (SELECT p.goods_received_note_id FROM payment p WHERE UPPER(p.payment_status) = 'PARTIAL' OR p.deleted = true)",
      countQuery =
          "SELECT COUNT(c) FROM (SELECT * FROM goods_received_note grn WHERE grn.payment_date < current_date AND grn.id NOT IN (SELECT p.goods_received_note_id FROM payment p WHERE p.deleted = false) "
              + "UNION SELECT * FROM goods_received_note grn WHERE grn.payment_date < current_date AND grn.id IN (SELECT p.goods_received_note_id FROM payment p WHERE UPPER(p.payment_status) = 'PARTIAL' OR p.deleted = true)) as c",
      nativeQuery = true)
  Page<GRNView> findGrnWithPaymentDateExceeded(Pageable pageable);
}
