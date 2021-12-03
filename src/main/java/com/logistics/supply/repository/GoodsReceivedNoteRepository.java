package com.logistics.supply.repository;

import com.logistics.supply.model.GoodsReceivedNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Repository
public interface GoodsReceivedNoteRepository extends JpaRepository<GoodsReceivedNote, Long> {
  List<GoodsReceivedNote> findBySupplier(int supplierId);

  List<GoodsReceivedNote> findByApprovedByHodIsFalse();

  List<GoodsReceivedNote> findByApprovedByGmIsFalse();

  List<GoodsReceivedNote> findByApprovedByGmIsTrue();


  @Query(
      value =
          "SELECT * from goods_received_note grn where grn.invoice_id =:invoiceId",
      nativeQuery = true)
  GoodsReceivedNote findByInvoiceId(@Param("invoiceId") int invoiceId);

  @Query(
      value =
          "SELECT count(*) from goods_received_note grn where DATE(grn.created_date) = CURRENT_DATE",
      nativeQuery = true)
  int findCountOfGRNForToday();

  @Query(value = "select * from goods_received_note grn where grn.id not in (SELECT p.goods_received_note_id from payment p) " +
          "and grn.invoice_id in (select id from invoice i2 where i2.payment_date <= current_date + interval '7 day') " +
          "union (select * from goods_received_note grn where grn.id in " +
          "(SELECT p.goods_received_note_id from payment p where UPPER(p.payment_status) != UPPER('COMPLETED') ))", nativeQuery = true)
  List<GoodsReceivedNote> findNumberOfPaymentDueInOneWeek();

  @Query(
      value =
          "SELECT\n"
              + "\tgrn.id,\n"
              + "\t(\n"
              + "\tSELECT\n"
              + "\t\tri.name\n"
              + "\tfrom\n"
              + "\t\trequest_item ri\n"
              + "\twhere\n"
              + "\t\tri.id = lpori.request_items_id) as request_item,\n"
              + "\t(\n"
              + "\tSELECT\n"
              + "\t\tname\n"
              + "\tfrom\n"
              + "\t\tsupplier s\n"
              + "\twhere\n"
              + "\t\ts.id = grn.supplier) as supplier,\n"
              + "\t(\n"
              + "\tselect\n"
              + "\t\ti.invoice_number\n"
              + "\tfrom\n"
              + "\t\tinvoice i\n"
              + "\twhere\n"
              + "\t\ti.id = grn.invoice_id) as invoice_number,\n"
              + "\tDATE(grn.created_date) as date_received\n"
              + "from\n"
              + "\tgoods_received_note grn\n"
              + "join local_purchase_order_request_items lpori on\n"
              + "\tgrn.local_purchase_order_id = lpori.local_purchase_order_id\n"
              + "where\n"
              + "\tgrn.created_date BETWEEN CAST(:startDate AS DATE) and CAST(:endDate AS DATE)\n"
              + "order by\n"
              + "\tgrn.created_date DESC ",
      nativeQuery = true)
  List<Object[]> getGoodsReceivedNoteReport(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query(
      value =
          "SELECT * from goods_received_note grn where grn.id not in (SELECT p.goods_received_note_id from payment p)"
              + " UNION SELECT * from goods_received_note grn where grn.id in (SELECT p.goods_received_note_id from payment p where UPPER(p.payment_status) = 'PARTIAL')",
      nativeQuery = true)
  List<GoodsReceivedNote> grnWithoutCompletePayment();


  List<GoodsReceivedNote> findByPaymentDateIsNullAndApprovedByGmTrueAndApprovedByHodTrue();
}
