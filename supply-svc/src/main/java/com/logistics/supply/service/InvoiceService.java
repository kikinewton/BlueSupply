package com.logistics.supply.service;

import com.logistics.supply.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.logistics.supply.exception.InvoiceNotFoundException;
import com.logistics.supply.model.Invoice;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {
  private final InvoiceRepository invoiceRepository;

  @Cacheable(key = "#invoiceId")
  public Invoice findByInvoiceId(int invoiceId) {
    return invoiceRepository
        .findById(invoiceId)
        .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
  }

  public Invoice findByInvoiceNo(String invoiceNo) {
    return invoiceRepository
        .findByInvoiceNumber(invoiceNo)
        .orElseThrow(() -> new InvoiceNotFoundException(invoiceNo));
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
    return invoiceRepository.findBySupplierId(supplierId);
  }

  public Invoice saveInvoice(Invoice invoice) {
    return invoiceRepository.save(invoice);
  }
}
