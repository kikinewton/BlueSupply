package com.logistics.supply.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import com.logistics.supply.model.FloatOrder;

@Getter
public class FloatEvent extends ApplicationEvent {

  private final String isEndorsed;
  private FloatOrder floatOrder;

  public FloatEvent(Object source, FloatOrder order) {
    super(source);
    this.floatOrder = order;
    this.isEndorsed = order.getEndorsement().getEndorsementStatus();
  }
}
