package com.logistics.supply.repository;

import com.logistics.supply.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

  List<Invoice> findBySupplierId(int supplierId);

  @Query(value = "SELECT * from invoice i where i.invoice_number LIKE %:invoiceNo%", nativeQuery = true)
  Invoice findByInvoiceNo(@Param("invoiceNo") String invoiceNo);
}
