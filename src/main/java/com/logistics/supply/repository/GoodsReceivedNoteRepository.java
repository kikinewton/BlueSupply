package com.logistics.supply.repository;

import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.LocalPurchaseOrder;
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

  List<GoodsReceivedNote> findByApprovedByGmFalseAndApprovedByHodTrue();


  Optional<GoodsReceivedNote> findByLocalPurchaseOrder(LocalPurchaseOrder localPurchaseOrder);

  @Query(
      value = "SELECT * from goods_received_note grn where grn.invoice_id =:invoiceId",
      nativeQuery = true)
  GoodsReceivedNote findByInvoiceId(@Param("invoiceId") int invoiceId);

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
          "select * from goods_received_note grn where grn.id not in (SELECT p.goods_received_note_id from payment p) and grn.payment_date <= current_date + interval '7 day' union (select * from goods_received_note grn where grn.id in (SELECT p.goods_received_note_id from payment p where UPPER(p.payment_status) != UPPER('COMPLETED')))",
      nativeQuery = true)
  List<GoodsReceivedNote> findPaymentDueInOneWeek();

  @Query(
      value =
          "select * from grn_report g where g.date_received BETWEEN CAST(:startDate AS DATE) and CAST(:endDate AS DATE) order by g.id desc",
      nativeQuery = true)
  List<Object[]> getGoodsReceivedNoteReport(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query(
      value =
          "SELECT * from goods_received_note grn where grn.id not in (SELECT p.goods_received_note_id from payment p) and grn.payment_date is not null"
              + " UNION SELECT * from goods_received_note grn where grn.id in (SELECT p.goods_received_note_id from payment p where UPPER(p.payment_status) = 'PARTIAL')",
      nativeQuery = true)
  List<GoodsReceivedNote> grnWithoutCompletePayment();

  List<GoodsReceivedNote> findByPaymentDateIsNullAndApprovedByGmTrueAndApprovedByHodTrue();
}
