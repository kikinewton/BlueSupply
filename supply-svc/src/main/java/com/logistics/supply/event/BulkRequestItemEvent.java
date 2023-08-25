package com.logistics.supply.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import com.logistics.supply.model.RequestItem;
import java.util.List;

@Getter
public class BulkRequestItemEvent extends ApplicationEvent {

  private List<RequestItem> requestItems;
  private final String isEndorsed;

  public BulkRequestItemEvent(Object source, List<RequestItem> requestItems) {
    super(source);
    this.requestItems = requestItems;
    this.isEndorsed =
        requestItems.stream().map(RequestItem::getEndorsement).findFirst().get().toString();
  }
}
