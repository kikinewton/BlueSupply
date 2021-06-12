package com.logistics.supply.service;

import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
      Optional<BigDecimal> amountPaid = paymentRepository.findTotalPaidAmountByPurchaseNumber(purchaseNumber);
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
      Optional<Payment> payment = paymentRepository.findByChequeNumber(chequeNumber);
      if (payment.isPresent()) return payment.get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void updatePaymentStatus(PaymentStatus paymentStatus, String purchaseNumber) {
    paymentRepository.updatePaymentStatus(paymentStatus.toString(), purchaseNumber);
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

  public Payment findById(int paymentId) {
    try {
      Optional<Payment> payment = paymentRepository.findById(paymentId);
      if (payment.isPresent()) return payment.get();

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<Payment> findAllPayment(long periodStart, long periodEnd) {
    List<Payment> payments = new ArrayList<>();
    try {
      Date startDate = new java.util.Date(periodStart);
      Date endDate = new java.util.Date(periodEnd);
      payments.addAll(paymentRepository.findAllByCreatedDateBetween(startDate, endDate));
      return payments;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return payments;
  }
}
