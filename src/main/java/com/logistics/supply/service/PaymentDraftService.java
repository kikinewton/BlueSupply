package com.logistics.supply.service;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentDraftRepository;
import com.logistics.supply.repository.PaymentRepository;
import com.logistics.supply.specification.PaymentDraftSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentDraftService {

  @Autowired PaymentDraftRepository paymentDraftRepository;
  @Autowired PaymentRepository paymentRepository;
  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  @PersistenceContext
  EntityManager entityManager;

  public PaymentDraft savePaymentDraft(PaymentDraft draft) {
    return paymentDraftRepository.save(draft);
  }

  @Transactional(rollbackFor = Exception.class)
  public Payment approvalByAuditor(int paymentDraftId, String status) {
    Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
    if (draft.isPresent()) {
      try {
        paymentDraftRepository.approvePaymentDraft(Boolean.parseBoolean(status), paymentDraftId);
        PaymentDraft result = findByDraftId(paymentDraftId);
        if (Boolean.parseBoolean(status)) {
          System.out.println("Convert draft to actual payment");
          Payment payment = acceptPaymentDraft(result);
          paymentDraftRepository.deleteById(result.getId());
          return payment;
        }
      } catch (Exception e) {
        log.error(e.toString());
      }
    }
    return null;
  }

  public Payment approvePaymentDraft(int paymentDraftId, boolean status) {
    try {
      CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
      CriteriaUpdate<PaymentDraft> update = criteriaBuilder.createCriteriaUpdate(PaymentDraft.class);
      Root e = update.from(PaymentDraft.class);
      update.set("approvalFromAuditor", status);
      update.where(criteriaBuilder.equal(e.get("id"), paymentDraftId));
      this.entityManager.createQuery(update).executeUpdate();
      PaymentDraft updatedDraft = findByDraftId(paymentDraftId);
      if(updatedDraft.getApprovalFromAuditor()) {
        Payment payment = acceptPaymentDraft(updatedDraft);
        paymentDraftRepository.deleteById(updatedDraft.getId());
        return payment;
      }
    }catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public PaymentDraft updatePaymentDraft(int paymentDraftId, PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
    if (draft.isPresent()) {
      PaymentDraft d = draft.get();
      Optional<GoodsReceivedNote> grn =
          goodsReceivedNoteRepository.findById(paymentDraftDTO.getGoodsReceivedNote().getId());

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

  @Transactional(rollbackFor = Exception.class)
  private Payment acceptPaymentDraft(PaymentDraft paymentDraft) {
    System.out.println("paymentDraft = " + paymentDraft.toString());
    Payment payment = new Payment();
    payment.setPaymentAmount(paymentDraft.getPaymentAmount());
    payment.setPaymentMethod(paymentDraft.getPaymentMethod());
    payment.setBank(paymentDraft.getBank());
    payment.setGoodsReceivedNote(paymentDraft.getGoodsReceivedNote());
    payment.setChequeNumber(paymentDraft.getChequeNumber());
    payment.setPaymentStatus(paymentDraft.getPaymentStatus());
    payment.setPaymentDraftId(paymentDraft.getId());
    payment.setPurchaseNumber(paymentDraft.getPurchaseNumber());
    payment.setApprovalFromAuditor(paymentDraft.getApprovalFromAuditor());
    payment.setWithHoldingTaxAmount(paymentDraft.getWithHoldingTaxAmount());
    try {
      Payment p = paymentRepository.save(payment);
      return p;
    } catch (Exception e) {
      log.error(e.toString());
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

  public List<PaymentDraft> findAllDrafts(int pageNo, int pageSize) {
    List<PaymentDraft> drafts = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
      drafts.addAll(paymentDraftRepository.findAll(pageable).getContent());
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
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
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
