package com.logistics.supply.service;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.event.listener.PaymentDraftListener;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import com.logistics.supply.specification.PaymentDraftSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  @Autowired ApplicationEventPublisher applicationEventPublisher;

  public PaymentDraft savePaymentDraft(PaymentDraft draft) {
    return paymentDraftRepository.save(draft);
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
                PaymentDraft result = approveByAuthority(employeeRole, pd);
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

  private PaymentDraft approveByAuthority(EmployeeRole employeeRole, PaymentDraft pd) {
    switch (employeeRole) {
      case ROLE_AUDITOR:
        pd.setApprovalFromAuditor(true);
        pd.setApprovalByAuditorDate(new Date());
        return paymentDraftRepository.save(pd);
      case ROLE_FINANCIAL_MANAGER:
        pd.setApprovalFromFM(true);
        pd.setApprovalByFMDate(new Date());
        return paymentDraftRepository.save(pd);
      case ROLE_GENERAL_MANAGER:
        pd.setApprovalFromGM(true);
        pd.setApprovalByGMDate(new Date());
        return paymentDraftRepository.save(pd);
      default:
        return null;
    }
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
          Page<PaymentDraft> draftPageFM = paymentDraftRepository.findAll(pdsStatus, pageable);
          drafts.addAll(draftPageFM.getContent());
          return drafts;

        case ROLE_GENERAL_MANAGER:
          pdsStatus.add(new SearchCriteria("approvalFromFM", true, SearchOperation.EQUAL));
          Page<PaymentDraft> draftPageGM = paymentDraftRepository.findAll(pdsStatus, pageable);
          drafts.addAll(draftPageGM.getContent());
          return drafts;
      }
      return drafts;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return drafts;
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
      e.printStackTrace();
      log.error(e.toString());
    }
    return drafts;
  }
}
