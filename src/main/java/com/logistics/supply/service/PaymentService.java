package com.logistics.supply.service;

import com.logistics.supply.dto.CancelPaymentDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.event.listener.CancelPaymentEventListener;
import com.logistics.supply.model.Payment;
import com.logistics.supply.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
  private final ApplicationEventPublisher applicationEventPublisher;
  private final PaymentRepository paymentRepository;

  @Cacheable(value = "paymentBySupplierId", key = "supplierId")
  public List<Payment> findPaymentsToSupplier(int supplierId) {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentRepository.findAllPaymentToSupplier(supplierId));
      return payments;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return payments;
  }


  @Cacheable(value = "paymentByPN", key = "purchaseNumber")
  public List<Payment> findByPurchaseNumber(String purchaseNumber) {
    List<Payment> payment = new ArrayList<>();
    try {
      payment.addAll(paymentRepository.findByPurchaseNumber(purchaseNumber));
      return payment;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return payment;
  }

  public long count() {
    return paymentRepository.count() + 1;
  }

  public BigDecimal findTotalPaymentMadeByPurchaseNumber(String purchaseNumber) {
    try {
      Optional<BigDecimal> amountPaid =
          paymentRepository.findTotalPaidAmountByPurchaseNumber(purchaseNumber);
      if (amountPaid.isPresent()) return amountPaid.get();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }


  public Payment findByCheque(String chequeNumber) {
    try {
      Optional<Payment> payment = paymentRepository.findByChequeNumber(chequeNumber);
      if (payment.isPresent()) return payment.get();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  @CacheEvict(value = "{allPayments, paymentById, paymentByInvoiceNo, paymentBySupplierId}", allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  public Payment cancelPayment(String chequeNumber) {
    try {
      paymentRepository.cancelPayment(PaymentStatus.CANCELLED.getPaymentStatus(), chequeNumber);
      Optional<Payment> payment = paymentRepository.findByChequeNumber(chequeNumber);
      if (payment.isPresent() && payment.get().getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
        return payment.get();
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e.toString());
    }
    return null;
  }

  @CacheEvict(value = "{allPayments, paymentById, paymentByInvoiceNo, paymentBySupplierId}", allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  public Payment cancelPayment(CancelPaymentDTO cancelPaymentDTO) {
    try {
      paymentRepository.cancelPayment(
          PaymentStatus.CANCELLED.getPaymentStatus(), cancelPaymentDTO.getChequeNumber());
      Optional<Payment> payment =
          paymentRepository.findByChequeNumber(cancelPaymentDTO.getChequeNumber());
      if (payment.isPresent() && payment.get().getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
        CompletableFuture.runAsync(
            () -> {
              CancelPaymentEventListener.CancelPaymentEvent event =
                  new CancelPaymentEventListener.CancelPaymentEvent(
                      this, payment.get(), cancelPaymentDTO.getComment());
              applicationEventPublisher.publishEvent(event);
            });
        return payment.get();
      }
    } catch (Exception e) {
      log.error(e.toString());
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
      log.error(e.getMessage());
    }
    return null;
  }

  public List<Payment> findPaymentsForCurrentMonth() {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentRepository.findAllPaymentMadeThisMonth());
      return payments;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return payments;
  }

//  public List<Payment> findPaymentMadeToday() {
//    List<Payment> payments = new ArrayList<>();
//    try {
//      payments.addAll(paymentRepository.findAllPaymentMadeToday());
//      return payments;
//    } catch (Exception e) {
//      log.error(e.getMessage());
//    }
//    return payments;
//  }

  @Cacheable(value = "paymentByInvoiceNo", key = "{#invoiceNumber}")
  public List<Payment> findByInvoiceNumber(String invoiceNumber) {
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentRepository.findByInvoiceNumber(invoiceNumber));
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return payments;
  }

  @Cacheable(value = "paymentById", key = "{#paymentId}")
  public Payment findById(int paymentId) {
    try {
      Optional<Payment> payment = paymentRepository.findById(paymentId);
      if (payment.isPresent()) return payment.get();

    } catch (Exception e) {
      log.error(e.getMessage());
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
      log.error(e.getMessage());
    }
    return payments;
  }

  public List<Payment> findAllPayment(int pageNo, int pageSize) {
    List<Payment> payments = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      payments.addAll(paymentRepository.findAll(pageable).getContent());
      return payments;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return payments;
  }

  public Collection<? extends Payment> findPaymentsDueWithinOneWeek() {
    return paymentRepository.findPaymentsDueWithinOneWeek();
  }
}
