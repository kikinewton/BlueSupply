package com.logistics.supply.repository;

import com.logistics.supply.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    List<Invoice> findBySupplierId(int supplierId);

    Invoice findByInvoiceNumber(String invoiceNo);
}

