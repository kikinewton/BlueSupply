package com.logistics.supply.service;

import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.repository.InvoiceRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.logistics.supply.util.Constants.INVOICE_NOT_FOUND;

@Service
@Slf4j
public class InvoiceService {

  @Autowired InvoiceRepository invoiceRepository;

  @SneakyThrows
//  @Cacheable(key = "#invoiceId")
  public Invoice findByInvoiceId(int invoiceId) {
    return invoiceRepository
        .findById(invoiceId)
        .orElseThrow(() -> new GeneralException(INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public Invoice findByInvoiceNo(String invoiceNo) {
    return invoiceRepository
        .findByInvoiceNumber(invoiceNo)
        .orElseThrow(() -> new GeneralException(INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));
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
    }
    return null;
  }
}
