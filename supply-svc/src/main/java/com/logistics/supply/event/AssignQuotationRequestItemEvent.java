package com.logistics.supply.event;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import com.logistics.supply.model.RequestItem;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class AssignQuotationRequestItemEvent extends ApplicationEvent {
    private final List<RequestItem> requestItems;
    private final int hasQuotation;

    public AssignQuotationRequestItemEvent(Object source, List<RequestItem> requestItems) {
        super(source);
        this.requestItems = requestItems;
        this.hasQuotation = requestItems.stream().map(RequestItem::getQuotations).collect(Collectors.toSet()).size();

    }
}
