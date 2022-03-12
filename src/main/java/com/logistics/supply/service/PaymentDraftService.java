package com.logistics.supply.service;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.enums.PaymentStatus;
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
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentDraftService {

  @Autowired PaymentDraftRepository paymentDraftRepository;
  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  @Autowired EmployeeRepository employeeRepository;
  @Autowired ApplicationEventPublisher applicationEventPublisher;

  public PaymentDraft savePaymentDraft(PaymentDraft draft) {
    try {
      return paymentDraftRepository.save(draft);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public long count() {
    return paymentDraftRepository.count() + 1;
  }

  @Transactional(rollbackFor = Exception.class)
  public PaymentDraft approvePaymentDraft(int paymentDraftId, EmployeeRole employeeRole) {
    try {
      return Optional.of(findByDraftId(paymentDraftId))
          .map(
              pd -> {
                PaymentDraft result = approveByAuthority(employeeRole, paymentDraftId);
                if (employeeRole.equals(EmployeeRole.ROLE_GENERAL_MANAGER)) {
                  PaymentDraftListener.PaymentDraftEvent paymentDraftEvent =
                      new PaymentDraftListener.PaymentDraftEvent(this, result);
                  applicationEventPublisher.publishEvent(paymentDraftEvent);
                }
                return result;
              })
          .orElse(null);

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  private PaymentDraft approveByAuthority(EmployeeRole employeeRole, int paymentDraftId) {
    try {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      String username = ((UserDetails) principal).getUsername();
      Employee employee = employeeRepository.findByEmailAndEnabledIsTrue(username).get();
      var draft =
          paymentDraftRepository
              .findById(paymentDraftId)
              .map(
                  paymentDraft -> {
                    switch (employeeRole) {
                      case ROLE_AUDITOR:
                        return auditorApproval(employeeRole, employee, paymentDraft);
                      case ROLE_FINANCIAL_MANAGER:
                        return financialManagerApproval(employeeRole, employee, paymentDraft);
                      case ROLE_GENERAL_MANAGER:
                        return generalManagerApproval(employeeRole, employee, paymentDraft);
                    }
                    return null;
                  });
      if (draft.isPresent()) return draft.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  private PaymentDraft generalManagerApproval(
      EmployeeRole employeeRole, Employee employee, PaymentDraft paymentDraft) {
    paymentDraft.setApprovalFromGM(true);
    paymentDraft.setApprovalByGMDate(new Date());
    paymentDraft.setEmployeeGmId(
        employee.getRoles().contains(employeeRole) ? employee.getId() : null);
    return paymentDraftRepository.save(paymentDraft);
  }

  private PaymentDraft financialManagerApproval(
      EmployeeRole employeeRole, Employee employee, PaymentDraft paymentDraft) {
    paymentDraft.setApprovalFromFM(true);
    paymentDraft.setApprovalByFMDate(new Date());
    paymentDraft.setEmployeeFmId(
        employee.getRoles().contains(employeeRole) ? employee.getId() : null);
    System.out.println("employeeRole = " + employee.getRoles().contains(employeeRole));
    return paymentDraftRepository.save(paymentDraft);
  }

  private PaymentDraft auditorApproval(
      EmployeeRole employeeRole, Employee employee, PaymentDraft paymentDraft) {
    paymentDraft.setApprovalFromAuditor(true);
    paymentDraft.setApprovalByAuditorDate(new Date());
    paymentDraft.setEmployeeAuditorId(
        employee.getRoles().contains(employeeRole) ? employee.getId() : null);
    return paymentDraftRepository.save(paymentDraft);
  }

  @Transactional(rollbackFor = Exception.class)
  public PaymentDraft updatePaymentDraft(int paymentDraftId, PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
    if (draft.isPresent()) {
      PaymentDraft d = draft.get();
      Optional<GoodsReceivedNote> grn =
          goodsReceivedNoteRepository.findById(
              Long.valueOf(paymentDraftDTO.getGoodsReceivedNote().getId()));

      BeanUtils.copyProperties(paymentDraftDTO, d);
      grn.ifPresent(d::setGoodsReceivedNote);

      try {
        return paymentDraftRepository.save(d);
      } catch (Exception e) {
        log.error(e.toString());
      }
    }
    return null;
  }

  public PaymentDraft findByDraftId(int paymentDraftId) {
    try {
      Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
      return draft.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
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

  public Page<PaymentDraft> paymentDraftHistory(
      int pageNo, int pageSize, EmployeeRole employeeRole) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      PaymentDraftSpecification pdsStatus = new PaymentDraftSpecification();
      switch (employeeRole) {
        case ROLE_AUDITOR:
          pdsStatus.add(new SearchCriteria("approvalFromAuditor", true, SearchOperation.EQUAL));
          Page<PaymentDraft> draftPage = paymentDraftRepository.findAll(pdsStatus, pageable);
          if (draftPage != null) return draftPage;

        case ROLE_FINANCIAL_MANAGER:
          pdsStatus.add(new SearchCriteria("approvalFromAuditor", true, SearchOperation.EQUAL));
          pdsStatus.add(new SearchCriteria("approvalFromFM", true, SearchOperation.EQUAL));
          Page<PaymentDraft> draftPageFM = paymentDraftRepository.findAll(pdsStatus, pageable);
          if (draftPageFM != null) return draftPageFM;

        case ROLE_GENERAL_MANAGER:
          pdsStatus.add(new SearchCriteria("approvalFromFM", true, SearchOperation.EQUAL));
          pdsStatus.add(new SearchCriteria("approvalFromGM", true, SearchOperation.EQUAL));
          Page<PaymentDraft> draftPageGM = paymentDraftRepository.findAll(pdsStatus, pageable);
          if (draftPageGM != null) return draftPageGM;
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
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
