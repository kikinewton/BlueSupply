package com.logistics.supply.event.listener;

import com.logistics.supply.model.Payment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.repository.PaymentRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentDraftListener {
  private final PaymentRepository paymentRepository;

  @Async
  @EventListener(
      condition =
          "#paymentDraftEvent.getPaymentDraft().getApprovalFromGM() == true && #paymentDraftEvent.getPaymentDraft().getApprovalFromAuditor() == true && #paymentDraftEvent.getPaymentDraft().getApprovalFromFM() == true")
  public void addPayment(PaymentDraftEvent paymentDraftEvent) {
    Payment payment = new Payment();
    PaymentDraft paymentDraft = paymentDraftEvent.getPaymentDraft();
    BeanUtils.copyProperties(paymentDraft, payment);
    payment.setPaymentDraftId(paymentDraft.getId());
    payment.setDeleted(false);
    try {
      paymentRepository.save(payment);
      log.info("====== PAYMENT ADDED ======");
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  @Getter
  public final static class PaymentDraftEvent extends ApplicationEvent {
    private final PaymentDraft paymentDraft;

    public PaymentDraftEvent(Object source, PaymentDraft paymentDraft) {
      super(source);
      this.paymentDraft = paymentDraft;
    }
  }
}
