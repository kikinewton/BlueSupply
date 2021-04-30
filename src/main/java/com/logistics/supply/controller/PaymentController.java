package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class PaymentController extends AbstractRestService {

  @GetMapping(value = "/payments/{paymentId}")
  public ResponseDTO<Payment> findPaymentById(@PathVariable("paymentId") int paymentId) {
    try {
      Payment payment = paymentService.findById(paymentId);
      if (Objects.nonNull(payment))
        return new ResponseDTO<>(HttpStatus.OK.name(), payment, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/payments/supplier/{supplierId}")
  public ResponseDTO<List<Payment>> findPaymentBySupplier(
      @PathVariable("supplierId") int supplierId) {
    List<Payment> payments = new ArrayList<>();
    try {
      Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
      if (supplier.isPresent()) payments.addAll(paymentService.findPaymentsToSupplier(supplierId));
      return new ResponseDTO<>(HttpStatus.OK.name(), payments, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.OK.name(), payments, ERROR);
  }

  @GetMapping(value = "payments/invoice/{invoiceNum}")
  public ResponseDTO<List<Payment>> findPaymentByInvoiceNumber(
      @PathVariable("invoiceNum") String invoiceNum) {
    List<Payment> payments = new ArrayList<>();
    try {
      Invoice i = invoiceService.findByInvoiceNo(invoiceNum);
      if (Objects.nonNull(i)) payments.addAll(paymentService.findByInvoiceNumber(invoiceNum));
      return new ResponseDTO<>(HttpStatus.OK.name(), payments, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), payments, ERROR);
  }

  @GetMapping(value = "payments/purchaseNumber/{purchaseNumber}")
  public ResponseDTO<List<Payment>> findPaymentByPurchaseNumber(
      @PathVariable("purchaseNumber") String purchaseNumber) {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentService.findByPurchaseNumber(purchaseNumber));
      return new ResponseDTO<>(HttpStatus.OK.name(), payments, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), payments, ERROR);
  }
}
