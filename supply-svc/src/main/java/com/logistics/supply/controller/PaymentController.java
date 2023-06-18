package com.logistics.supply.controller;

import com.logistics.supply.dto.CancelPaymentDTO;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.service.PaymentService;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.logistics.supply.model.Payment;
import com.logistics.supply.service.SupplierService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class PaymentController {
  private final SupplierService supplierService;
  private final PaymentService paymentService;

  @GetMapping(value = "/payments/{paymentId}")
  public ResponseEntity<?> findPaymentById(@PathVariable("paymentId") int paymentId) {
    try {
      Payment payment = paymentService.findById(paymentId);
      if (Objects.nonNull(payment)) {
        ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", Constants.SUCCESS, payment);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return Helper.failedResponse("FETCH FAILED");
  }

  @GetMapping(value = "/payments/supplier/{supplierId}")
  public ResponseEntity<?> findPaymentBySupplier(@PathVariable("supplierId") int supplierId) {
    List<Payment> payments = new ArrayList<>();
    try {
      boolean supplierExist = supplierService.existById(supplierId);
      if (supplierExist) payments.addAll(paymentService.findPaymentsToSupplier(supplierId));
      ResponseDto response = new ResponseDto<>("FETCH SUCCESSFUL", Constants.SUCCESS, payments);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return Helper.failedResponse("FETCH FAILED");
  }

  @GetMapping(value = "/payments/invoice/{invoiceNum}")
  public ResponseEntity<?> findPaymentByInvoiceNumber(
      @PathVariable("invoiceNum") String invoiceNum) {
    try {
      List<Payment> payments = paymentService.findByInvoiceNumber(invoiceNum);
      return ResponseDto.wrapSuccessResult(payments, Constants.FETCH_SUCCESSFUL);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return Helper.failedResponse("FETCH FAILED");
  }

  @GetMapping(value = "/payments/purchaseNumber/{purchaseNumber}")
  public ResponseEntity<?> findPaymentByPurchaseNumber(
      @PathVariable("purchaseNumber") String purchaseNumber) {
    try {
      List<Payment> payments = paymentService.findByPurchaseNumber(purchaseNumber);
      return ResponseDto.wrapSuccessResult(payments, Constants.FETCH_SUCCESSFUL);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return Helper.failedResponse("FETCH FAILED");
  }

  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  @PutMapping(value = "/payments/{paymentId}/cancelCheque")
  public ResponseEntity<?> cancelCheque(@RequestBody CancelPaymentDTO cancelPaymentDTO)
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
    List<Payment> payments = new ArrayList<>();
    if (invoiceNumber.isPresent()) {
      payments.addAll(paymentService.findByInvoiceNumber(invoiceNumber.get()));
      return ResponseDto.wrapSuccessResult(payments, Constants.FETCH_SUCCESSFUL);
    }
    if (supplierId.isPresent()) {
      boolean supplierExist = supplierService.existById(supplierId.get());
      if (supplierExist) payments.addAll(paymentService.findPaymentsToSupplier(supplierId.get()));
      return ResponseDto.wrapSuccessResult(payments, Constants.FETCH_SUCCESSFUL);
    }
    List<Payment> allPayment = paymentService.findAll(pageNo, pageSize);
    return ResponseDto.wrapSuccessResult(allPayment, Constants.FETCH_SUCCESSFUL);
  }
}
