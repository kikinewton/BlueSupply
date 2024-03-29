package com.logistics.supply.service;

import com.logistics.supply.model.GoodsReceivedNote;
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

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.listener.PaymentDraftListener;
import com.logistics.supply.exception.PaymentDraftNotFoundException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import com.logistics.supply.specification.PaymentDraftSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDraftService {

  private final PaymentDraftRepository paymentDraftRepository;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;

  private final EmployeeRepository employeeRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @CacheEvict(
      value = {"paymentDraftHistory"},
      allEntries = true)
  public PaymentDraft savePaymentDraft(PaymentDraft draft) throws GeneralException {
    try {
      return paymentDraftRepository.save(draft);
    } catch (ConstraintViolationException e) {
//      throw new ConstraintViolationException(e.getLocalizedMessage(), e.getSQLException(), e.getConstraintName());
      log.error(e.toString());
      throw new GeneralException(e.getConstraintName() + " already exist", HttpStatus.BAD_REQUEST);
    }
  }

  public long count() {
    return paymentDraftRepository.countAll() + 1;
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"paymentDraftHistory"},
      allEntries = true)
  public PaymentDraft approvePaymentDraft(int paymentDraftId, EmployeeRole employeeRole)
      throws GeneralException {
    PaymentDraft result = approveByAuthority(employeeRole, paymentDraftId);
    if (EmployeeRole.ROLE_GENERAL_MANAGER.equals(employeeRole)) {
      CompletableFuture.runAsync(
          () -> {
            PaymentDraftListener.PaymentDraftEvent paymentDraftEvent =
                new PaymentDraftListener.PaymentDraftEvent(this, result);
            applicationEventPublisher.publishEvent(paymentDraftEvent);
          });
    }
    return result;
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"paymentDraftHistory"},
      allEntries = true)
  private PaymentDraft approveByAuthority(EmployeeRole employeeRole, int paymentDraftId)
      throws GeneralException {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    Employee employee = employeeRepository.findByEmailAndEnabledIsTrue(username).get();

    PaymentDraft paymentDraft =
        paymentDraftRepository
            .findById(paymentDraftId)
            .orElseThrow(() -> new PaymentDraftNotFoundException(paymentDraftId));
    switch (employeeRole) {
      case ROLE_AUDITOR:
        return auditorApproval(employeeRole, employee, paymentDraft);
      case ROLE_FINANCIAL_MANAGER:
        return financialManagerApproval(employeeRole, employee, paymentDraft);
      case ROLE_GENERAL_MANAGER:
        return generalManagerApproval(employeeRole, employee, paymentDraft);
    }
    throw new GeneralException("PAYMENT APPROVAL FAILED", HttpStatus.FORBIDDEN);
  }

  private PaymentDraft generalManagerApproval(
      EmployeeRole employeeRole, Employee employee, PaymentDraft paymentDraft) {
    paymentDraft.setApprovalFromGM(true);
    paymentDraft.setApprovalByGMDate(new Date());
    paymentDraft.setEmployeeGmId(employee.getId());
    return paymentDraftRepository.save(paymentDraft);
  }

  private PaymentDraft financialManagerApproval(
      EmployeeRole employeeRole, Employee employee, PaymentDraft paymentDraft) {
    paymentDraft.setApprovalFromFM(true);
    paymentDraft.setApprovalByFMDate(new Date());
    paymentDraft.setEmployeeFmId(employee.getId());

    return paymentDraftRepository.save(paymentDraft);
  }

  private PaymentDraft auditorApproval(
      EmployeeRole employeeRole, Employee employee, PaymentDraft paymentDraft) {
    paymentDraft.setApprovalFromAuditor(true);
    paymentDraft.setApprovalByAuditorDate(new Date());
    paymentDraft.setEmployeeAuditorId(employee.getId());
    return paymentDraftRepository.save(paymentDraft);
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"paymentDraftHistory"},
      allEntries = true)
  public PaymentDraft updatePaymentDraft(int paymentDraftId, PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    PaymentDraft draft =
        paymentDraftRepository
            .findById(paymentDraftId)
            .orElseThrow(() -> new PaymentDraftNotFoundException(paymentDraftId));
    GoodsReceivedNote grn =
        goodsReceivedNoteRepository
            .findById(Long.valueOf(paymentDraftDTO.getGoodsReceivedNote().getId()))
            .orElseThrow(() -> new PaymentDraftNotFoundException((int) paymentDraftDTO.getGoodsReceivedNote().getId()));

    BeanUtils.copyProperties(paymentDraftDTO, draft);
    draft.setGoodsReceivedNote(grn);
    return paymentDraftRepository.save(draft);
  }

  public PaymentDraft findByDraftId(int paymentDraftId) throws GeneralException {
    return paymentDraftRepository
        .findById(paymentDraftId)
        .orElseThrow(() -> new PaymentDraftNotFoundException(paymentDraftId));
  }

  public List<PaymentDraft> findAllDrafts(int pageNo, int pageSize, EmployeeRole employeeRole) {
    List<PaymentDraft> drafts = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      PaymentDraftSpecification pdsStatus = new PaymentDraftSpecification();
      switch (employeeRole) {
        case ROLE_AUDITOR:
          pdsStatus.add(new SearchCriteria("approvalFromAuditor", true, SearchOperation.IS_NULL));
          Page<PaymentDraft> draftPage = paymentDraftRepository.findAll(pdsStatus, pageable);
          drafts.addAll(draftPage.getContent());
          return drafts;

        case ROLE_FINANCIAL_MANAGER:
          pdsStatus.add(new SearchCriteria("approvalFromAuditor", true, SearchOperation.EQUAL));
          pdsStatus.add(new SearchCriteria("approvalFromFM", null, SearchOperation.IS_NULL));
          Page<PaymentDraft> draftPageFM = paymentDraftRepository.findAll(pdsStatus, pageable);
          drafts.addAll(draftPageFM.getContent());
          return drafts;

        case ROLE_GENERAL_MANAGER:
          pdsStatus.add(new SearchCriteria("approvalFromFM", true, SearchOperation.EQUAL));
          pdsStatus.add(new SearchCriteria("approvalFromGM", null, SearchOperation.IS_NULL));
          Page<PaymentDraft> draftPageGM = paymentDraftRepository.findAll(pdsStatus, pageable);
          drafts.addAll(draftPageGM.getContent());
          return drafts;
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return drafts;
  }

  @Cacheable(value = "paymentDraftHistory", key = "{#pageNo,#pageSize, #employeeRole}")
  public Page<PaymentDraft> paymentDraftHistory(int pageNo, int pageSize, EmployeeRole employeeRole)
      throws GeneralException {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      PaymentDraftSpecification pdsStatus = new PaymentDraftSpecification();
      switch (employeeRole) {
        case ROLE_AUDITOR:
          pdsStatus.add(new SearchCriteria("approvalFromAuditor", true, SearchOperation.EQUAL));
          return paymentDraftRepository.findAll(pdsStatus, pageable);

        case ROLE_FINANCIAL_MANAGER:
          pdsStatus.add(new SearchCriteria("approvalFromAuditor", true, SearchOperation.EQUAL));
          pdsStatus.add(new SearchCriteria("approvalFromFM", true, SearchOperation.EQUAL));
          return paymentDraftRepository.findAll(pdsStatus, pageable);

        case ROLE_GENERAL_MANAGER:
          pdsStatus.add(new SearchCriteria("approvalFromFM", true, SearchOperation.EQUAL));
          pdsStatus.add(new SearchCriteria("approvalFromGM", true, SearchOperation.EQUAL));
          return paymentDraftRepository.findAll(pdsStatus, pageable);

        default:
          return paymentDraftRepository.findAll(pageable);
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("DRAFTS NOT FOUND", HttpStatus.BAD_REQUEST);
  }

  public List<PaymentDraft> findByStatus(PaymentStatus status, int pageNo, int pageSize) {
    List<PaymentDraft> drafts = new ArrayList<>();
    try {
      PaymentDraftSpecification pdsStatus = new PaymentDraftSpecification();
      pdsStatus.add(new SearchCriteria("paymentStatus", status, SearchOperation.EQUAL));
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      Page<PaymentDraft> draftPage = paymentDraftRepository.findAll(pdsStatus, pageable);
      drafts.addAll(draftPage.getContent());
      return drafts;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return drafts;
  }

  @CacheEvict(
      value = {"paymentDraftHistory"},
      allEntries = true)
  public void deleteById(int paymentDraftId) {
    paymentDraftRepository.deleteById(paymentDraftId);
  }
}
