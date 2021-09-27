package com.logistics.supply.event;

import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AssignQuotationRequestItemEvent extends ApplicationEvent {
    private List<RequestItem> requestItems;
    private final int hasQuotation;

    public AssignQuotationRequestItemEvent(Object source, List<RequestItem> requestItems) throws Exception {
        super(source);
        this.requestItems = requestItems;
        this.hasQuotation = requestItems.stream().map(RequestItem::getSuppliers).collect(Collectors.toSet()).size();

    }
}
