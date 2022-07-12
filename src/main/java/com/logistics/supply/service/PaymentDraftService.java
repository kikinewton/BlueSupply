package com.logistics.supply.service;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.listener.PaymentDraftListener;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import com.logistics.supply.specification.PaymentDraftSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.logistics.supply.util.Constants.GRN_NOT_FOUND;
import static com.logistics.supply.util.Constants.PAYMENT_DRAFT_NOT_FOUND;

@Slf4j
@Service
public class PaymentDraftService {

  @Autowired PaymentDraftRepository paymentDraftRepository;
  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  @Autowired EmployeeRepository employeeRepository;
  @Autowired ApplicationEventPublisher applicationEventPublisher;

  @CacheEvict(value = {"paymentDraftHistory"}, allEntries = true)
  public PaymentDraft savePaymentDraft(PaymentDraft draft) {
    return paymentDraftRepository.save(draft);
  }

  public long count() {
    return paymentDraftRepository.countAll() + 1;
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = {"paymentDraftHistory"}, allEntries = true)
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
  @CacheEvict(value = {"paymentDraftHistory"}, allEntries = true)
  private PaymentDraft approveByAuthority(EmployeeRole employeeRole, int paymentDraftId)
      throws GeneralException {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    Employee employee = employeeRepository.findByEmailAndEnabledIsTrue(username).get();

    PaymentDraft paymentDraft =
        paymentDraftRepository
            .findById(paymentDraftId)
            .orElseThrow(() -> new GeneralException(PAYMENT_DRAFT_NOT_FOUND, HttpStatus.NOT_FOUND));
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
  @CacheEvict(value = {"paymentDraftHistory"}, allEntries = true)
  public PaymentDraft updatePaymentDraft(int paymentDraftId, PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    PaymentDraft draft =
        paymentDraftRepository
            .findById(paymentDraftId)
            .orElseThrow(() -> new GeneralException(PAYMENT_DRAFT_NOT_FOUND, HttpStatus.NOT_FOUND));
    GoodsReceivedNote grn =
        goodsReceivedNoteRepository
            .findById(Long.valueOf(paymentDraftDTO.getGoodsReceivedNote().getId()))
            .orElseThrow(() -> new GeneralException(GRN_NOT_FOUND, HttpStatus.NOT_FOUND));

    BeanUtils.copyProperties(paymentDraftDTO, draft);
    draft.setGoodsReceivedNote(grn);
    return paymentDraftRepository.save(draft);
  }

  public PaymentDraft findByDraftId(int paymentDraftId) throws GeneralException {
    return paymentDraftRepository
        .findById(paymentDraftId)
        .orElseThrow(() -> new GeneralException(PAYMENT_DRAFT_NOT_FOUND, HttpStatus.NOT_FOUND));
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
}
