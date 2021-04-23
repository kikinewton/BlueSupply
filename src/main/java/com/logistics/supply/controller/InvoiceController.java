package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class InvoiceController extends AbstractRestService {

  @PostMapping(value = "/stores/invoice")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseDTO<Invoice> addInvoice(@RequestBody Invoice invoice) {
    boolean docExist =
        requestDocumentService.verifyIfDocExist(invoice.getInvoiceDocument().getId());
    if (!docExist)
      return new ResponseDTO<>(
          HttpStatus.BAD_REQUEST.name(), null, "INVOICE_DOCUMENT_DOES_NOT_EXIST");
    Invoice i = invoiceService.saveInvoice(invoice);
    if (Objects.isNull(i)) return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    return new ResponseDTO<>(HttpStatus.OK.name(), i, SUCCESS);
  }

  @GetMapping(value = "/invoice/supplier/{supplierId}")
  public ResponseDTO<List<Invoice>> findInvoiceBySupplier(
      @PathVariable("supplierId") int supplierId) {
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
    if (!supplier.isPresent())
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "SUPPLIER_DOES_NOT_EXIST");
    List<Invoice> invoices = invoiceService.findBySupplier(supplierId);
    if (invoices.size() > 0) return new ResponseDTO<>(HttpStatus.OK.name(), invoices, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/invoice/{invoiceNo}")
  public ResponseDTO<Invoice> findByInvoiceNo(@PathVariable("invoiceNo") String invoiceNo) {
    Invoice invoice = invoiceService.findByInvoiceNo(invoiceNo);
    if (Objects.isNull(invoice))
      return new ResponseDTO<>(
          HttpStatus.BAD_REQUEST.name(), null, "INVOICE_NUMBER_DOES_NOT_EXIST");
    return new ResponseDTO<>(HttpStatus.OK.name(), invoice, SUCCESS);
  }

  @GetMapping(value = "/invoice/all")
  public ResponseDTO<List<Invoice>> findAllInvoice() {
    List<Invoice> invoices = invoiceService.findAllInvoice();
    if (invoices.size() > 0) return new ResponseDTO<>(HttpStatus.OK.name(), invoices, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }
}