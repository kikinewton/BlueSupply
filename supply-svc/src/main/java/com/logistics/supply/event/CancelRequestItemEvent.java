package com.logistics.supply.event;

import com.logistics.supply.model.CancelledRequestItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class CancelRequestItemEvent extends ApplicationEvent {

    private List<CancelledRequestItem> cancelledRequestItems;

    public CancelRequestItemEvent(Object source, List<CancelledRequestItem> cancelledRequestItems) {
        super(source);
        this.cancelledRequestItems = cancelledRequestItems;
    }

}
