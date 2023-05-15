package com.logistics.supply.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PostPersist;
import com.logistics.supply.model.Payment;
import com.logistics.supply.repository.PaymentDraftRepository;

@Slf4j
@RequiredArgsConstructor
public class PaymentListener {
  private final PaymentDraftRepository paymentDraftRepository;

  @PostPersist
  public void deletePaymentDraft(Payment payment) {
    log.info("=== delete draft related to payment ===");
    paymentDraftRepository.deleteById(payment.getPaymentDraftId());
  }
}
