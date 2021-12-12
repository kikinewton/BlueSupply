package com.logistics.supply.event.listener;

import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.repository.PaymentRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentDraftListener {

  private final PaymentRepository paymentRepository;

  @Async
  @EventListener(
      condition =
          "#paymentDraftEvent.paymentDraft.getApprovalFromGM() == true && #paymentDraftEvent.paymentDraft.getApprovalFromAuditor() == true && #paymentDraftEvent.paymentDraft.getApprovalFromFM() == true")
  public void addPayment(PaymentDraftEvent paymentDraftEvent) {
    Payment payment = new Payment();
    PaymentDraft paymentDraft = paymentDraftEvent.getPaymentDraft();
    payment.setPaymentAmount(paymentDraft.getPaymentAmount());
    payment.setPaymentMethod(paymentDraft.getPaymentMethod());
    payment.setBank(paymentDraft.getBank());
    payment.setGoodsReceivedNote(paymentDraft.getGoodsReceivedNote());
    payment.setChequeNumber(paymentDraft.getChequeNumber());
    payment.setPaymentStatus(paymentDraft.getPaymentStatus());
    payment.setPaymentDraftId(paymentDraft.getId());
    payment.setPurchaseNumber(paymentDraft.getPurchaseNumber());
    payment.setApprovalFromAuditor(paymentDraft.getApprovalFromAuditor());
    try {
      Payment p = paymentRepository.save(payment);
      if(Objects.nonNull(p)) log.info("====== PAYMENT ADDED ======");
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  @Getter
  public static class PaymentDraftEvent extends ApplicationEvent {
    private PaymentDraft paymentDraft;

    public PaymentDraftEvent(Object source, PaymentDraft paymentDraft) {
      super(source);
      this.paymentDraft = paymentDraft;
    }
  }
}
