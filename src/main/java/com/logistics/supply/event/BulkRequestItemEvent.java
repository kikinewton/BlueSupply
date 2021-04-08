package com.logistics.supply.event;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class BulkRequestItemEvent extends ApplicationEvent {

  private List<RequestItem> requestItems;
  private final EndorsementStatus isEndorsed;

  public BulkRequestItemEvent(Object source, List<RequestItem> requestItems) throws Exception {
    super(source);
    this.requestItems = requestItems;
    this.isEndorsed = requestItems.stream().map(RequestItem::getEndorsement).findFirst().orElseThrow(Exception::new);

  }
}
