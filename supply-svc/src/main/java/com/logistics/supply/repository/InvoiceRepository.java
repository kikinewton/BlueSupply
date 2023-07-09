package com.logistics.supply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.Invoice;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

  List<Invoice> findBySupplierId(int supplierId);

  Optional<Invoice> findByInvoiceNumber(String invoiceNo);
}
