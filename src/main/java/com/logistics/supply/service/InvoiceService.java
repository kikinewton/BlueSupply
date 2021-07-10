package com.logistics.supply.service;

import com.logistics.supply.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.hibernate.id.enhanced.InitialValueAwareOptimizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class InvoiceService extends AbstractDataService {


  public Invoice findByInvoiceId(int invoiceId) {
    try {
      var inv =  invoiceRepository.findById(invoiceId);
      if (inv.isPresent()) return inv.get();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public Invoice findByInvoiceNo(String invoiceNo) {
    try {
      return invoiceRepository.findByInvoiceNo(invoiceNo);
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
