package com.logistics.supply.controller;

import com.logistics.supply.dto.InvoiceDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class InvoiceController extends AbstractRestService {

  @PostMapping(value = "/invoice")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> addInvoice(@RequestBody InvoiceDTO invoice) {
    boolean docExist =
        requestDocumentService.verifyIfDocExist(invoice.getInvoiceDocument().getId());
    if (!docExist) failedResponse("INVOICE DOCUMENT DOES NOT EXIST");
    Invoice inv = new Invoice();
    BeanUtils.copyProperties(invoice, inv);
    Invoice i = invoiceService.saveInvoice(inv);
    if (Objects.isNull(i)) return failedResponse("ADD INVOICE FAILED");
    ResponseDTO response = new ResponseDTO<>("ADD INVOICE SUCCESSFUL", SUCCESS, i);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/invoice/supplier/{supplierId}")
  public ResponseEntity<?> findInvoiceBySupplier(
      @PathVariable("supplierId") int supplierId) {
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
    if (!supplier.isPresent())
      return failedResponse("SUPPLIER DOES NOT EXIST");
    List<Invoice> invoices = invoiceService.findBySupplier(supplierId);
    if (invoices.size() > 0)  {
      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS,invoices);
      return ResponseEntity.ok(response);
    }

    return failedResponse("INVOICE FOR SUPPLIER DOES NOT EXIST");
  }

  @GetMapping(value = "/invoices/{invoiceNo}")
  public ResponseEntity<?> findByInvoiceNo(@PathVariable("invoiceNo") String invoiceNo) {
    Invoice invoice = invoiceService.findByInvoiceNo(invoiceNo);
    if (Objects.isNull(invoice))
      return failedResponse("INVOICE NUMBER DOES NOT EXIST");
    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS,invoice);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/invoices")
  public ResponseEntity<?> findAllInvoice(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    List<Invoice> invoices = invoiceService.findAllInvoice(pageNo, pageSize);
    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, invoices);
    return ResponseEntity.ok(response);
  }


}
