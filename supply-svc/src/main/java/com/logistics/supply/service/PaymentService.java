package com.logistics.supply.service;

import com.logistics.supply.dto.CancelPaymentDTO;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.listener.CancelPaymentEventListener;
import com.logistics.supply.model.Payment;
import com.logistics.supply.repository.PaymentRepository;
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
    return paymentRepository.findAllPaymentToSupplier(supplierId);
  }

  @Cacheable(value = "paymentByPN", key = "purchaseNumber")
  public List<Payment> findByPurchaseNumber(String purchaseNumber) {
    return paymentRepository.findByPurchaseNumber(purchaseNumber);
  }

  public long count() {
    return paymentRepository.countAll() + 1;
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

  public Payment findByCheque(String chequeNumber) throws GeneralException {
    return paymentRepository
        .findByChequeNumber(chequeNumber)
        .orElseThrow(() -> new NotFoundException("Payment with cheque number %s not found".formatted(chequeNumber)));
  }

  @CacheEvict(
      value = {"allPayments", "paymentById", "paymentByInvoiceNo", "paymentBySupplierId"},
      allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  public Payment cancelPayment(String chequeNumber) throws GeneralException {
    try {
      paymentRepository.cancelPayment(PaymentStatus.CANCELLED.getPaymentStatus(), chequeNumber);
      Optional<Payment> payment = paymentRepository.findByChequeNumber(chequeNumber);
      if (payment.isPresent() && payment.get().getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
        return payment.get();
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.CANCEL_PAYMENT_FAILED, HttpStatus.BAD_REQUEST);
  }

  @CacheEvict(
      value = {"allPayments", "paymentById", "paymentByInvoiceNo", "paymentBySupplierId"},
      allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  public Payment cancelPayment(CancelPaymentDTO cancelPaymentDTO) throws GeneralException {
    try {
      paymentRepository.cancelPayment(
          PaymentStatus.CANCELLED.getPaymentStatus(), cancelPaymentDTO.getChequeNumber());
      Optional<Payment> payment =
          paymentRepository.findByChequeNumberIncludeDeleted(cancelPaymentDTO.getChequeNumber());
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
    throw new GeneralException("CANCEL PAYMENT FAILED", HttpStatus.BAD_REQUEST);
  }

  public void updatePaymentStatus(PaymentStatus paymentStatus, String purchaseNumber) {
    paymentRepository.updatePaymentStatus(paymentStatus.toString(), purchaseNumber);
  }

  public List<Payment> findByPaymentStatus(PaymentStatus paymentStatus) {
    return paymentRepository.findByPaymentStatusOrderByCreatedDate(paymentStatus);
  }

  public List<Payment> findPaymentsForCurrentMonth() {
    return paymentRepository.findAllPaymentMadeThisMonth();
  }

  @Cacheable(
      value = "paymentByInvoiceNo",
      key = "#invoiceNumber",
      unless = "#result == #result.isEmpty()")
  public List<Payment> findByInvoiceNumber(String invoiceNumber) {
    return paymentRepository.findByInvoiceNumber(invoiceNumber);
  }

  @Cacheable(value = "paymentById", key = "#paymentId")
  public Payment findById(int paymentId) throws GeneralException {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new GeneralException(Constants.PAYMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public List<Payment> findAllPayment(long periodStart, long periodEnd) {
    List<Payment> payments = new ArrayList<>();
    try {
      Date startDate = new Date(periodStart);
      Date endDate = new Date(periodEnd);
      payments.addAll(paymentRepository.findAllByCreatedDateBetween(startDate, endDate));
      return payments;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return payments;
  }

  public List<Payment> findAll(int pageNo, int pageSize) {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return paymentRepository.findAllPayments();
  }

  public Collection<? extends Payment> findPaymentsDueWithinOneWeek() {
    return paymentRepository.findPaymentsDueWithinOneWeek();
  }
}
