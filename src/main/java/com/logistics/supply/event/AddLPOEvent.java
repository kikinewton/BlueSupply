package com.logistics.supply.event;

import com.logistics.supply.model.LocalPurchaseOrder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AddLPOEvent extends ApplicationEvent {

  private LocalPurchaseOrder lpo;

  public AddLPOEvent(Object source, LocalPurchaseOrder lpo) {
    super(source);
    this.lpo = lpo;
  }
}
