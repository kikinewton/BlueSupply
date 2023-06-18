package com.logistics.supply.controller;

import com.logistics.supply.dto.InvoiceDTO;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.service.RequestDocumentService;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.logistics.supply.model.Invoice;
import com.logistics.supply.service.InvoiceService;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class InvoiceController {

  private final InvoiceService invoiceService;
  private final RequestDocumentService requestDocumentService;

  @PostMapping(value = "/invoice")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> addInvoice(@RequestBody InvoiceDTO invoice) {
    boolean docExist =
        requestDocumentService.verifyIfDocExist(invoice.getInvoiceDocument().getId());
    if (!docExist) Helper.failedResponse("INVOICE DOCUMENT DOES NOT EXIST");
    Invoice inv = new Invoice();
    BeanUtils.copyProperties(invoice, inv);
    Invoice i = invoiceService.saveInvoice(inv);
    if (Objects.isNull(i)) return Helper.failedResponse("ADD INVOICE FAILED");
    ResponseDto response = new ResponseDto<>("ADD INVOICE SUCCESSFUL", Constants.SUCCESS, i);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/invoice/supplier/{supplierId}")
  public ResponseEntity<?> findInvoiceBySupplier(@PathVariable("supplierId") int supplierId) {
    List<Invoice> invoices = invoiceService.findBySupplier(supplierId);
    if (invoices.size() > 0) {
      ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", Constants.SUCCESS, invoices);
      return ResponseEntity.ok(response);
    }

    return Helper.failedResponse("INVOICE FOR SUPPLIER DOES NOT EXIST");
  }

  @GetMapping(value = "/invoices/{invoiceNo}")
  public ResponseEntity<?> findByInvoiceNo(@PathVariable("invoiceNo") String invoiceNo) {
    Invoice invoice = invoiceService.findByInvoiceNo(invoiceNo);
    if (Objects.isNull(invoice)) return Helper.failedResponse("INVOICE NUMBER DOES NOT EXIST");
    ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", Constants.SUCCESS, invoice);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/invoices")
  public ResponseEntity<?> findAllInvoice(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    List<Invoice> invoices = invoiceService.findAllInvoice(pageNo, pageSize);
    ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", Constants.SUCCESS, invoices);
    return ResponseEntity.ok(response);
  }
}
