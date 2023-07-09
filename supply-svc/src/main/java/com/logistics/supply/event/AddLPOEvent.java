package com.logistics.supply.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import com.logistics.supply.model.LocalPurchaseOrderDraft;

@Getter
public class AddLPOEvent extends ApplicationEvent {

  private LocalPurchaseOrderDraft lpo;

  public AddLPOEvent(Object source, LocalPurchaseOrderDraft lpo) {
    super(source);
    this.lpo = lpo;
  }
}
