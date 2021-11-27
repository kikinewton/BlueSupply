package com.logistics.supply.event.listener;

import com.logistics.supply.model.Payment;
import com.logistics.supply.repository.PaymentDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PostPersist;

@Slf4j
@RequiredArgsConstructor
public class PaymentListener {
  final PaymentDraftRepository paymentDraftRepository;

  @PostPersist
  public void deletePaymentDraft(Payment payment) {
    log.info("=== delete draft related to payment ===");
    paymentDraftRepository.deleteById(payment.getPaymentDraftId());
  }
}
