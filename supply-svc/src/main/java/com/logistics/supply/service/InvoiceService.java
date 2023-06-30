package com.logistics.supply.service;

import com.logistics.supply.dto.InvoiceDto;
import com.logistics.supply.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.logistics.supply.exception.InvoiceNotFoundException;
import com.logistics.supply.model.Invoice;

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

  public Page<Invoice> findAllInvoice(int pageNo, int pageSize) {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("created_date").descending());
      return invoiceRepository.findAll(pageable);
  }

  public List<Invoice> findBySupplier(int supplierId) {
    return invoiceRepository.findBySupplierId(supplierId);
  }

  public Invoice saveInvoice(InvoiceDto invoiceDto) {

    log.info("Save invoice");
    Invoice invoice = new Invoice();
    BeanUtils.copyProperties(invoiceDto, invoice);
    return invoiceRepository.save(invoice);
  }
}
