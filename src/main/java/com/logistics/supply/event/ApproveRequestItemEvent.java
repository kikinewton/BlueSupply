package com.logistics.supply.event;

import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import javax.validation.constraints.Size;
import java.util.List;

@Getter
public class ApproveRequestItemEvent extends ApplicationEvent {

  private List<RequestItem> requestItems;
  private String isApproved;

  public ApproveRequestItemEvent(Object source, @Size(min = 1) List<RequestItem> requestItems) {
    super(source);
    this.requestItems = requestItems;
    this.isApproved =
        requestItems.stream().map(RequestItem::getApproval).findFirst().get().toString();
  }
}
