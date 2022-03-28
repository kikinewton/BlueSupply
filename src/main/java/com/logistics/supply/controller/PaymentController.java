package com.logistics.supply.controller;

import com.logistics.supply.dto.CancelPaymentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Payment;
import com.logistics.supply.service.PaymentService;
import com.logistics.supply.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

@RestController
@Slf4j
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class PaymentController {

  final SupplierService supplierService;
  final PaymentService paymentService;

  @GetMapping(value = "/payments/{paymentId}")
  public ResponseEntity<?> findPaymentById(@PathVariable("paymentId") int paymentId) {
    try {
      Payment payment = paymentService.findById(paymentId);
      if (Objects.nonNull(payment)) {
        ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, payment);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH FAILED");
  }

  @GetMapping(value = "/payments/supplier/{supplierId}")
  public ResponseEntity<?> findPaymentBySupplier(@PathVariable("supplierId") int supplierId) {
    List<Payment> payments = new ArrayList<>();
    try {
      boolean supplierExist = supplierService.existById(supplierId);
      if (supplierExist) payments.addAll(paymentService.findPaymentsToSupplier(supplierId));
      ResponseDTO response = new ResponseDTO<>("FETCH SUCCESSFUL", SUCCESS, payments);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH FAILED");
  }

  @GetMapping(value = "/payments/invoice/{invoiceNum}")
  public ResponseEntity<?> findPaymentByInvoiceNumber(
      @PathVariable("invoiceNum") String invoiceNum) {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentService.findByInvoiceNumber(invoiceNum));
      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, payments);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH FAILED");
  }

  @GetMapping(value = "/payments/purchaseNumber/{purchaseNumber}")
  public ResponseEntity<?> findPaymentByPurchaseNumber(
      @PathVariable("purchaseNumber") String purchaseNumber) {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentService.findByPurchaseNumber(purchaseNumber));
      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, payments);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH FAILED");
  }

  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  @PutMapping(value = "/payments/{paymentId}/cancelCheque")
  public ResponseEntity<?> cancelCheque(
      @RequestBody CancelPaymentDTO cancelPaymentDTO, @PathVariable("paymentId") int paymentId) {

    Payment payment = paymentService.cancelPayment(cancelPaymentDTO);
    if (payment == null) return failedResponse("CANCEL PAYMENT FAILED");
    ResponseDTO response = new ResponseDTO("CANCEL PAYMENT SUCCESSFUL", SUCCESS, payment);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/payments")
  public ResponseEntity<?> findPayments(
      @RequestParam(required = false) Optional<String> invoiceNumber,
      @RequestParam(required = false) Optional<Integer> supplierId,
      @RequestParam(defaultValue = "0", required = false) int pageNo,
      @RequestParam(defaultValue = "200", required = false) int pageSize) {
    List<Payment> payments = new ArrayList<>();
    if (invoiceNumber.isPresent()) {
      payments.addAll(paymentService.findByInvoiceNumber(invoiceNumber.get()));
      ResponseDTO response = new ResponseDTO("FETCH PAYMENT BY INVOICE SUCCESSFUL", SUCCESS, payments);
      return ResponseEntity.ok(response);
    }
    if (supplierId.isPresent()) {
      boolean supplierExist = supplierService.existById(supplierId.get());
      if (supplierExist)
        payments.addAll(paymentService.findPaymentsToSupplier(supplierId.get()));
      ResponseDTO response = new ResponseDTO<>("FETCH PAYMENT BY SUPPLIER SUCCESSFUL", SUCCESS, payments);
      return ResponseEntity.ok(response);
    }
    payments.addAll(paymentService.findAllPayment(pageNo, pageSize));
    ResponseDTO response = new ResponseDTO("FETCH PAYMENT SUCCESSFUL", SUCCESS, payments);
    return ResponseEntity.ok(response);
  }
}
