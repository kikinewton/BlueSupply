package com.logistics.supply.service;

import com.logistics.supply.model.Invoice;
import com.logistics.supply.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.hibernate.id.enhanced.InitialValueAwareOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InvoiceService  {

  @Autowired
  InvoiceRepository invoiceRepository;

  public Invoice findByInvoiceId(int invoiceId) {
    try {
      Optional<Invoice> inv = invoiceRepository.findById(invoiceId);
      if (inv.isPresent()) return inv.get();
    }
    catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    return null;
  }

  public Invoice findByInvoiceNo(String invoiceNo) {
    try {
      return invoiceRepository.findByInvoiceNo(invoiceNo);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public List<Invoice> findAllInvoice(int pageNo, int pageSize) {
    List<Invoice> invoices = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("created_date").descending());
      invoices.addAll(invoiceRepository.findAll(pageable).getContent());
      return invoices;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return invoices;
  }

  public List<Invoice> findBySupplier(int supplierId) {
    List<Invoice> invoices = new ArrayList<>();
    try {
      invoices.addAll(invoiceRepository.findBySupplierId(supplierId));
      return invoices;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return invoices;
  }

  public Invoice saveInvoice(Invoice invoice) {
    try {
      return invoiceRepository.save(invoice);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }
}
