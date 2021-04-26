package com.logistics.supply.repository;

import com.logistics.supply.model.GoodsReceivedNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GoodsReceivedNoteRepository extends JpaRepository<GoodsReceivedNote, Integer> {
  List<GoodsReceivedNote> findBySupplier(int supplierId);

  @Query(
      value =
          "SELECT * from goods_received_note grn where grn.invoice_id in (SELECT i.id from invoice i where i.invoice_number =:invoiceNo)",
      nativeQuery = true)
  GoodsReceivedNote findByInvoiceNo(@Param("invoiceNo") String invoiceNo);
}
