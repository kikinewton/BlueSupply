package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

  @GetMapping(value = "payments/all")
  public ResponseDTO<List<Payment>> findAllPayments(
      @RequestParam(required = false) Long periodStart,
      @RequestParam(required = false) Long periodEnd,
      @RequestParam(required = false, defaultValue = "0") Integer supplierId,
      @RequestParam(required = false, defaultValue = "NA") String status) {
    List<Payment> payments = new ArrayList<>();
    Supplier supplier = null;
    if (Objects.isNull(periodStart))
      periodStart = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)).getTime();
    if (Objects.isNull(periodEnd)) periodEnd = new Date().getTime();
    if (!Integer.valueOf(supplierId).equals(0)) {
      supplier = supplierService.findBySupplierId(supplierId).get();
    }
    Map<String, List<Payment>> paymentsMap = new LinkedHashMap<>();
    try {
      payments.addAll(paymentService.findAllPayment(periodStart, periodEnd));
      paymentsMap.put("unfiltered", payments);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (payments.size() > 0) {
      System.out.println("size " + payments.size());
      payments.forEach(System.out::println);

      if (Objects.nonNull(supplier)) {

        List<Payment> filteredPayments =
            payments.stream()
                .filter(x -> x.getGoodsReceivedNote().getSupplier().equals(supplierId))
                .collect(Collectors.toList());
        paymentsMap.put("filtered", filteredPayments);
      }
      if (!status.equals("NA")) {
        List<Payment> paymentWithStatus =
            payments.stream()
                .filter(x -> x.getPaymentStatus().toString().equals(status))
                .collect(Collectors.toList());
        paymentsMap.put("statusPayment", paymentWithStatus);
      }

      if (Objects.nonNull(supplier) && !status.equals("NA")) {
        List<Payment> filteredPaymentWithStatus =
            paymentsMap.get("filtered").stream()
                .filter(x -> x.getPaymentStatus().toString().equals(status))
                .collect(Collectors.toList());
        paymentsMap.put("filteredWithStatus", filteredPaymentWithStatus);
      }
    }
    if (!status.equals("NA") && Objects.nonNull(supplier)) {
      System.out.println("paymentsMap = " + paymentsMap.get("filteredWithStatus"));
      return new ResponseDTO<>(
          HttpStatus.OK.name(), paymentsMap.get("filteredWithStatus"), SUCCESS);
    }
    if (!status.equals("NA"))
      return new ResponseDTO<>(HttpStatus.OK.name(), paymentsMap.get("statusPayment"), SUCCESS);

    if (Objects.nonNull(supplier))
      return new ResponseDTO<>(HttpStatus.OK.name(), paymentsMap.get("filtered"), SUCCESS);

    return new ResponseDTO<>(HttpStatus.OK.name(), payments, SUCCESS);
  }
}
