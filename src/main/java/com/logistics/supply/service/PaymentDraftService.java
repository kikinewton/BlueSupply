package com.logistics.supply.service;

import com.logistics.supply.model.PaymentDraft;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class PaymentDraftService extends AbstractDataService {

  public PaymentDraft savePaymentDraft(PaymentDraft draft) {
    return paymentDraftRepository.save(draft);
  }

  public PaymentDraft approvalByAuditor(long paymentDraftId, boolean status, String comment) {
    Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
    if (draft.isPresent()) {
      draft.get().setApprovalFromAuditor(status);
      draft.get().setAuditorComment(comment);
      draft.get().setApprovalByAuditorDate(new Date());
      return paymentDraftRepository.save(draft.get());
    }
    return null;
  }

//  public PaymentDraft approvalByGM(long paymentDraftId) {
//    Optional<PaymentDraft> draft = paymentDraftRepository.findById(paymentDraftId);
//    if(draft.isPresent()) {
//      draft.get().setApprovalByGeneralManagerDate(new Date());
//      draft.get().setApprovalFromGeneralManager(true);
//      return paymentDraftRepository.save(draft.get());
//    }
//    return null;
//  }
}
