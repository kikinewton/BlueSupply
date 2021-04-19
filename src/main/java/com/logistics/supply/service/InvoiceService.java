package com.logistics.supply.service;

import com.logistics.supply.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class InvoiceService extends AbstractDataService {

  public Invoice findByInvoiceNo(String invoiceNo) {
    try {
      return invoiceRepository.findByInvoiceNumber(invoiceNo);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public List<Invoice> findAllInvoice() {
    List<Invoice> invoices = new ArrayList<>();
    try {
      invoices.addAll(invoiceRepository.findAll());
      return invoices;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return invoices;
  }

  public List<Invoice> findBySupplier(int supplierId) {
    List<Invoice> invoices = new ArrayList<>();
    try {
      invoices.addAll(invoiceRepository.findBySupplier(supplierId));
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
