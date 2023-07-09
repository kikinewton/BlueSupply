package com.logistics.supply.event;

import com.logistics.supply.model.Payment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FullPaymentEvent extends ApplicationEvent {

    private Payment payment;

    public FullPaymentEvent(Object source, Payment payment) {
        super(source);
        this.payment = payment;
    }
}
