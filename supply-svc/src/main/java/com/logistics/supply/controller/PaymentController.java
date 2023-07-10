package com.logistics.supply.controller;

import com.logistics.supply.dto.CancelPaymentDto;
import com.logistics.supply.dto.PagedResponseDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Payment;
import com.logistics.supply.service.PaymentService;
import com.logistics.supply.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;

@RestController
@Slf4j
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class PaymentController {

  private final SupplierService supplierService;
  private final PaymentService paymentService;

  @GetMapping(value = "/payments/{paymentId}")
  public ResponseEntity<ResponseDto<Payment>> findPaymentById(@PathVariable("paymentId") int paymentId) {

    Payment payment = paymentService.findById(paymentId);
    return ResponseDto.wrapSuccessResult(payment, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/payments/supplier/{supplierId}")
  public ResponseEntity<PagedResponseDto<Page<Payment>>> findPaymentBySupplier(
          @PathVariable("supplierId") int supplierId,
          @RequestParam(defaultValue = "0", required = false) int pageNo,
          @RequestParam(defaultValue = "200", required = false) int pageSize) {

    supplierService.findBySupplierId(supplierId);
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    Page<Payment> paymentsToSupplier = paymentService.findPaymentsToSupplier(supplierId, pageable);
    return PagedResponseDto.wrapSuccessResult(paymentsToSupplier, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/payments/invoice/{invoiceNum}")
  public ResponseEntity<ResponseDto<List<Payment>>> findPaymentByInvoiceNumber(
      @PathVariable("invoiceNum") String invoiceNum) {

      List<Payment> payments = paymentService.findByInvoiceNumber(invoiceNum);
      return ResponseDto.wrapSuccessResult(payments, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/payments/purchaseNumber/{purchaseNumber}")
  public ResponseEntity<ResponseDto<List<Payment>>> findPaymentByPurchaseNumber(
      @PathVariable("purchaseNumber") String purchaseNumber) {

      List<Payment> payments = paymentService.findByPurchaseNumber(purchaseNumber);
      return ResponseDto.wrapSuccessResult(payments, FETCH_SUCCESSFUL);
  }

  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  @PutMapping(value = "/payments/{paymentId}/cancelCheque")
  public ResponseEntity<?> cancelCheque(@RequestBody CancelPaymentDto cancelPaymentDTO)
      throws GeneralException {
    Payment payment = paymentService.cancelPayment(cancelPaymentDTO);
    return ResponseDto.wrapSuccessResult(payment, "CANCEL PAYMENT SUCCESSFUL");
  }

  @GetMapping(value = "/payments")
  public ResponseEntity<?> findPayments(
      @RequestParam(required = false) Optional<String> invoiceNumber,
      @RequestParam(required = false) Optional<Integer> supplierId,
      @RequestParam(defaultValue = "0", required = false) int pageNo,
      @RequestParam(defaultValue = "200", required = false) int pageSize) {


    if (invoiceNumber.isPresent()) {
      List<Payment> payments = paymentService.findByInvoiceNumber(invoiceNumber.get());
      return ResponseDto.wrapSuccessResult(payments, FETCH_SUCCESSFUL);
    }

    if (supplierId.isPresent()) {
      supplierService.findBySupplierId(supplierId.get());
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      Page<Payment> paymentsToSupplier = paymentService.findPaymentsToSupplier(supplierId.get(), pageable);
      return PagedResponseDto.wrapSuccessResult(paymentsToSupplier, FETCH_SUCCESSFUL);
    }

    Page<Payment> allPayment = paymentService.findAll(pageNo, pageSize);
    return PagedResponseDto.wrapSuccessResult(allPayment, FETCH_SUCCESSFUL);
  }
}
