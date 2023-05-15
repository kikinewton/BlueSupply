package com.logistics.supply.event.listener;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.repository.PettyCashPaymentRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.logistics.supply.model.PettyCashPayment;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundsReceivedPettyCashListener {

  final PettyCashPaymentRepository pettyCashPaymentRepository;

  @EventListener
  public void addPettyCashPayment(FundsReceivedPettyCashEvent event) {
    try {
      event.pettyCashSet.forEach(
          p -> {
            if (p.isPaid()) return;
            PettyCashPayment payment =
                new PettyCashPayment(
                    p, event.paidBy, p.getAmount().multiply(BigDecimal.valueOf(p.getQuantity())));
            pettyCashPaymentRepository.save(payment);
          });
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  @Getter
  public static class FundsReceivedPettyCashEvent extends ApplicationEvent {
    private Employee paidBy;
    private List<PettyCash> pettyCashSet;

    public FundsReceivedPettyCashEvent(
        Object source, Employee paidBy, List<PettyCash> pettyCashSet) {
      super(source);
      this.paidBy = paidBy;
      this.pettyCashSet = pettyCashSet;
    }
  }
}
