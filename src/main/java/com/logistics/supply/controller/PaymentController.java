package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.PaymentService;
import com.logistics.supply.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
      Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
      if (supplier.isPresent()) payments.addAll(paymentService.findPaymentsToSupplier(supplierId));
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

  @PutMapping(value = "/payments/chequeNumber/{chequeNumber}")
  public ResponseEntity<?> cancelCheque(@PathVariable("chequeNumber") String chequeNumber) {
    try {
      Payment payment = paymentService.cancelPayment(chequeNumber);
      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, payment);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("CANCEL PAYMENT FAILED");
  }

  @GetMapping(value = "/payments")
  public ResponseEntity<?> findPayments(
      @RequestParam(required = false) String invoiceNumber,
      @RequestParam(defaultValue = "0", required = false) int supplierId,
      @RequestParam(defaultValue = "0", required = false) int pageNo,
      @RequestParam(defaultValue = "100", required = false) int pageSize) {
    List<Payment> payments = new ArrayList<>();
    try {
      if (invoiceNumber != null) {
        payments.addAll(paymentService.findByInvoiceNumber(invoiceNumber));
        ResponseDTO response = new ResponseDTO("FETCH BY INVOICE SUCCESSFUL", SUCCESS, payments);
        return ResponseEntity.ok(response);
      } else if (supplierId != 0) {
        Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
        if (supplier.isPresent())
          payments.addAll(paymentService.findPaymentsToSupplier(supplierId));
        ResponseDTO response = new ResponseDTO<>("FETCH BY SUPPLIER SUCCESSFUL", SUCCESS, payments);
        return ResponseEntity.ok(response);
      } else {
        payments.addAll(paymentService.findAllPayment(pageNo, pageSize));
        ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, payments);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH FAILED");
  }
}
