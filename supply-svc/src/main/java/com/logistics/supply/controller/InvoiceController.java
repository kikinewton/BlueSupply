package com.logistics.supply.controller;

import com.logistics.supply.dto.InvoiceDto;
import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.service.InvoiceService;
import com.logistics.supply.service.RequestDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;

@Slf4j
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class InvoiceController {

  private final InvoiceService invoiceService;
  private final RequestDocumentService requestDocumentService;

  @PostMapping(value = "/invoiceDto")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<ResponseDto<Invoice>> addInvoice(@Valid @RequestBody InvoiceDto invoiceDto) {

    requestDocumentService.verifyIfDocExist(invoiceDto.getInvoiceDocument().getId());
    Invoice invoice = invoiceService.saveInvoice(invoiceDto);
    return ResponseDto.wrapSuccessResult(invoice, "ADD INVOICE SUCCESSFUL");
  }

  @GetMapping(value = "/invoice/supplier/{supplierId}")
  public ResponseEntity<ResponseDto<List<Invoice>>> findInvoiceBySupplier(
          @PathVariable("supplierId") int supplierId) {

    List<Invoice> invoices = invoiceService.findBySupplier(supplierId);
    return ResponseDto.wrapSuccessResult(invoices, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/invoices/{invoiceNo}")
  public ResponseEntity<ResponseDto<Invoice>> findByInvoiceNo(
          @PathVariable("invoiceNo") String invoiceNo) {

    Invoice invoice = invoiceService.findByInvoiceNo(invoiceNo);
    return ResponseDto.wrapSuccessResult(invoice, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/invoices")
  public ResponseEntity<PagedResponseDTO<Page<Invoice>>> findAllInvoice(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {

    Page<Invoice> invoices = invoiceService.findAllInvoice(pageNo, pageSize);
    return PagedResponseDTO.wrapSuccessResult(invoices, FETCH_SUCCESSFUL);
  }
}
