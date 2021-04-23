package com.logistics.supply.service;

import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.Payment;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PaymentService extends AbstractDataService {

  public List<Payment> findPaymentsToSupplier(int supplierId) {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentRepository.findAllPaymentToSupplier(supplierId));
      return payments;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return payments;
  }

  public List<Payment> findByPurchaseNumber(String purchaseNumber) {
    List<Payment> payment = new ArrayList<>();
    try {
      payment.addAll(paymentRepository.findByPurchaseNumber(purchaseNumber));
      return payment;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return payment;
  }

  public BigDecimal findTotalPaymentMadeByPurchaseNumber(String purchaseNumber) {
    try {
      var amountPaid = paymentRepository.findTotalPaidAmountByPurchaseNumber(purchaseNumber);
      if (amountPaid.isPresent()) return amountPaid.get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String updateToFullPayment() {
    return null;
  }

  public Payment findByCheque(String chequeNumber) {
    try {
      var payment = paymentRepository.findByChequeNumber(chequeNumber);
      if (payment.isPresent()) return payment.get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<Payment> findByPaymentStatus(PaymentStatus paymentStatus) {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentRepository.findByPaymentStatusOrderByCreatedDate(paymentStatus));
      return payments;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<Payment> findPaymentsForCurrentMonth() {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentRepository.findAllPaymentMadeThisMonth());
      return payments;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return payments;
  }

  public List<Payment> findPaymentMadeToday() {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentRepository.findAllPaymentMadeToday());
      return payments;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return payments;
  }

  public List<Payment> findByInvoiceNumber(String invoiceNumber) {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentRepository.findByInvoiceNumber(invoiceNumber));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return payments;
  }
}
