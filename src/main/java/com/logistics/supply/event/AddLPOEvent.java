package com.logistics.supply.event;

import com.logistics.supply.model.LocalPurchaseOrder;
import org.springframework.context.ApplicationEvent;


public class AddLPOEvent extends ApplicationEvent {

  private LocalPurchaseOrder lpo;

  public AddLPOEvent(Object source, LocalPurchaseOrder lpo) {
    super(source);
    this.lpo = lpo;
  }
}
