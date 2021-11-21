package com.logistics.supply.event;

import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.LocalPurchaseOrderDraft;
import com.logistics.supply.service.LocalPurchaseOrderDraftService;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AddLPOEvent extends ApplicationEvent {

  private LocalPurchaseOrderDraft lpo;

  public AddLPOEvent(Object source, LocalPurchaseOrderDraft lpo) {
    super(source);
    this.lpo = lpo;
  }
}
