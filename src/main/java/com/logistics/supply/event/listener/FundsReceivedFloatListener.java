package com.logistics.supply.event.listener;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatPayment;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatPaymentRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundsReceivedFloatListener {

  final FloatPaymentRepository floatPaymentRepository;

  public void addFloatPayment(FundsReceivedFloatEvent event) {
    try {
      event.floats.forEach(
          f -> {
            FloatPayment payment =
                new FloatPayment(
                    f,
                    event.paidBy,
                    f.getEstimatedUnitPrice().multiply(BigDecimal.valueOf(f.getQuantity())));
            floatPaymentRepository.save(payment);
          });

      log.info("===== FLOAT FUNDS RECEIVED BY REQUESTER =====");
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  @Getter
  public static class FundsReceivedFloatEvent extends ApplicationEvent {
    private Employee paidBy;
    private Set<Floats> floats;

    public FundsReceivedFloatEvent(
        Object source, Employee paidBy, Set<Floats> floats) {
      super(source);
      this.paidBy = paidBy;
      this.floats = floats;
    }
  }
}
