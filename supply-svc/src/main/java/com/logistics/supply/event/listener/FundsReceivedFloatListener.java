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

@Slf4j
@Component
@RequiredArgsConstructor
public class FundsReceivedFloatListener {

  private final FloatPaymentRepository floatPaymentRepository;

  @EventListener
  public void addFloatPayment(FundsReceivedFloatEvent event) {

      FloatOrder floatOrder = event.getFloats();
      if (!floatOrder.isFundsReceived()) return;
    Employee paidBy = event.paidBy;
    FloatPayment payment = new FloatPayment(floatOrder, paidBy, floatOrder.getAmount());
      floatPaymentRepository.save(payment);
      log.info("Float order: {} requester received funds from {}", floatOrder.getFloatOrderRef(), paidBy.getEmail());
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
