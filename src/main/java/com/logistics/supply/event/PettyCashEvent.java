package com.logistics.supply.event;

import com.logistics.supply.model.PettyCash;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Set;

@Getter
public class PettyCashEvent extends ApplicationEvent {

    private Set<PettyCash> pettyCash;

    public PettyCashEvent(Object source, Set<PettyCash> pettyCash) {
        super(source);
        this.pettyCash = pettyCash;
    }
}
