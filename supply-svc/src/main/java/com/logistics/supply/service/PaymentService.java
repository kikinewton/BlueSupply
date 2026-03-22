package com.logistics.supply.service;

import com.logistics.supply.dto.CancelPaymentDto;
import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.enums.PaymentStage;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.exception.PaymentNotFoundException;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.listener.CancelPaymentEventListener;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.Payment;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
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
  private final EmployeeRepository employeeRepository;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;

  // -------------------------------------------------------------------------
  // Settled-payment read operations (existing)
  // -------------------------------------------------------------------------

  @Cacheable(value = "paymentBySupplierId", key = "#supplierId")
  public List<Payment> findPaymentsToSupplier(int supplierId) {
    return paymentRepository.findAllPaymentToSupplier(supplierId);
  }

  public Page<Payment> findPaymentsToSupplier(int supplierId, Pageable pageable) {
    return paymentRepository.findAllPaymentToSupplier(supplierId, pageable);
  }

  @Cacheable(value = "paymentByPN", key = "#purchaseNumber")
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
      paymentRepository.cancelPayment(PaymentStatus.CANCELLED.name(), chequeNumber);
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
  public Payment cancelPayment(CancelPaymentDto cancelPaymentDto) throws GeneralException {
    try {
      paymentRepository.cancelPayment(
          PaymentStatus.CANCELLED.name(), cancelPaymentDto.getChequeNumber());
      Optional<Payment> payment =
          paymentRepository.findByChequeNumberIncludeDeleted(cancelPaymentDto.getChequeNumber());

      if (payment.isPresent() && payment.get().getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
        CompletableFuture.runAsync(
            () -> {
              CancelPaymentEventListener.CancelPaymentEvent event =
                  new CancelPaymentEventListener.CancelPaymentEvent(
                      this, payment.get(), cancelPaymentDto.getComment());
              applicationEventPublisher.publishEvent(event);
            });
        return payment.get();
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("CANCEL PAYMENT FAILED", HttpStatus.BAD_REQUEST);
  }

  @Transactional(rollbackFor = Exception.class)
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
      unless = "#result == null || #result.isEmpty()")
  public List<Payment> findByInvoiceNumber(String invoiceNumber) {
    return paymentRepository.findByInvoiceNumber(invoiceNumber);
  }

  @Cacheable(value = "paymentById", key = "#paymentId")
  public Payment findById(int paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new PaymentNotFoundException(paymentId));
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

  public Page<Payment> findAll(int pageNo, int pageSize) {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return paymentRepository.findAllPayment(pageable);
  }

  public Collection<? extends Payment> findPaymentsDueWithinOneWeek() {
    return paymentRepository.findPaymentsDueWithinOneWeek();
  }

  // -------------------------------------------------------------------------
  // Draft workflow operations (merged from PaymentDraftService)
  // -------------------------------------------------------------------------

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = {"paymentDraftHistory", "requestStage"}, allEntries = true)
  public Payment savePaymentDraft(Payment draft) throws GeneralException {
    try {
      return paymentRepository.save(draft);
    } catch (ConstraintViolationException e) {
      log.error(e.toString());
      throw new GeneralException(e.getConstraintName() + " already exist", HttpStatus.BAD_REQUEST);
    }
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = {"paymentDraftHistory", "requestStage"}, allEntries = true)
  public Payment approvePaymentDraft(int paymentId, EmployeeRole employeeRole)
      throws GeneralException {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    Employee employee = employeeRepository.findByEmailAndEnabledIsTrue(username).get();

    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

    switch (employeeRole) {
      case ROLE_AUDITOR:
        payment.setApprovalFromAuditor(true);
        payment.setApprovalByAuditorDate(new Date());
        payment.setEmployeeAuditorId(employee.getId());
        payment.setStage(PaymentStage.AUDITOR_APPROVED);
        break;
      case ROLE_FINANCIAL_MANAGER:
        payment.setApprovalFromFM(true);
        payment.setApprovalByFMDate(new Date());
        payment.setEmployeeFmId(employee.getId());
        payment.setStage(PaymentStage.FM_APPROVED);
        break;
      case ROLE_GENERAL_MANAGER:
        payment.setApprovalFromGM(true);
        payment.setApprovalByGMDate(new Date());
        payment.setEmployeeGmId(employee.getId());
        payment.setStage(PaymentStage.FULLY_APPROVED);
        break;
      default:
        throw new GeneralException("PAYMENT APPROVAL FAILED", HttpStatus.FORBIDDEN);
    }
    return paymentRepository.save(payment);
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = {"paymentDraftHistory"}, allEntries = true)
  public Payment updatePaymentDraft(int paymentId, PaymentDraftDTO paymentDraftDTO) throws Exception {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    GoodsReceivedNote grn =
        goodsReceivedNoteRepository
            .findById(paymentDraftDTO.getGoodsReceivedNote().getId())
            .orElseThrow(() -> new PaymentNotFoundException((int) paymentDraftDTO.getGoodsReceivedNote().getId()));

    BeanUtils.copyProperties(paymentDraftDTO, payment, "id", "purchaseNumber", "stage",
        "withholdingTaxAmount", "withholdingTaxPercentage");
    payment.setGoodsReceivedNote(grn);
    return paymentRepository.save(payment);
  }

  public List<Payment> findAllDraftsByRole(int pageNo, int pageSize, EmployeeRole employeeRole) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    try {
      switch (employeeRole) {
        case ROLE_AUDITOR:
          return paymentRepository.findByStage(PaymentStage.DRAFT, pageable).getContent();
        case ROLE_FINANCIAL_MANAGER:
          return paymentRepository.findByStage(PaymentStage.AUDITOR_APPROVED, pageable).getContent();
        case ROLE_GENERAL_MANAGER:
          return paymentRepository.findByStage(PaymentStage.FM_APPROVED, pageable).getContent();
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  @Cacheable(value = "paymentDraftHistory", key = "{#pageNo,#pageSize, #employeeRole}")
  public Page<Payment> paymentDraftHistory(int pageNo, int pageSize, EmployeeRole employeeRole)
      throws GeneralException {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        return switch (employeeRole) {
            case ROLE_AUDITOR -> paymentRepository.findByStageIn(
                    List.of(PaymentStage.AUDITOR_APPROVED, PaymentStage.FM_APPROVED, PaymentStage.FULLY_APPROVED),
                    pageable);
            case ROLE_FINANCIAL_MANAGER -> paymentRepository.findByStageIn(
                    List.of(PaymentStage.FM_APPROVED, PaymentStage.FULLY_APPROVED), pageable);
            case ROLE_GENERAL_MANAGER -> paymentRepository.findByStage(PaymentStage.FULLY_APPROVED, pageable);
            default -> paymentRepository.findAllPayment(pageable);
        };
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("DRAFTS NOT FOUND", HttpStatus.BAD_REQUEST);
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = {"paymentDraftHistory"}, allEntries = true)
  public void deleteById(int paymentId) {
    paymentRepository.deleteById(paymentId);
  }
}
