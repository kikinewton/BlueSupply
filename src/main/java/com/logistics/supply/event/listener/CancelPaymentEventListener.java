package com.logistics.supply.event.listener;

import com.logistics.supply.email.EmailSender;
import com.logistics.supply.model.CancelPayment;
import com.logistics.supply.model.Payment;
import com.logistics.supply.repository.CancelPaymentRepository;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelPaymentEventListener {

  @Value("${config.templateMail}")
  String cancelPaymentEmail;

  private final EmailSender emailSender;
  private final CancelPaymentRepository cancelPaymentRepository;

  @EventListener(
      condition =
          "#cancelPaymentEvent.getPayment() != null && #cancelPaymentEvent.getComment() != null")
  public void cancelPayment(@NonNull CancelPaymentEvent cancelPaymentEvent) {
    CancelPayment cancelPayment =
        new CancelPayment(cancelPaymentEvent.getComment(), cancelPaymentEvent.getPayment());
    CancelPayment cancelled = cancelPaymentRepository.save(cancelPayment);
    if(cancelled == null) return;

  }

  @Getter
  @Setter
  public static class CancelPaymentEvent extends ApplicationEvent {
    private Payment payment;
    private String comment;

    public CancelPaymentEvent(Object source, Payment payment, String comment) {
      super(source);
      this.payment = payment;
      this.comment = comment;
    }
  }
}
