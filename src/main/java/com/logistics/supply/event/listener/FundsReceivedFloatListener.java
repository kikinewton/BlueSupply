package com.logistics.supply.event.listener;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.model.FloatPayment;
import com.logistics.supply.repository.FloatPaymentRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundsReceivedFloatListener {

  final FloatPaymentRepository floatPaymentRepository;

  @EventListener
  public void addFloatPayment(FundsReceivedFloatEvent event) {
    try {
      FloatOrder f = event.getFloats();
      if (f.isFundsReceived() == false) return;
      FloatPayment payment = new FloatPayment(f, event.paidBy, f.getAmount());
      FloatPayment p = floatPaymentRepository.save(payment);
      if (Objects.nonNull(p)) log.info("===== FLOAT FUNDS RECEIVED BY REQUESTER =====");

    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  @Getter
  public static class FundsReceivedFloatEvent extends ApplicationEvent {
    private Employee paidBy;
    private FloatOrder floats;

    public FundsReceivedFloatEvent(Object source, Employee paidBy, FloatOrder floatOrder) {
      super(source);
      this.paidBy = paidBy;
      this.floats = floatOrder;
    }
  }
}
