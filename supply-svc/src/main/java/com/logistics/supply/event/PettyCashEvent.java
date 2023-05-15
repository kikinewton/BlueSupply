package com.logistics.supply.event;

import com.logistics.supply.model.PettyCash;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;

import java.util.Set;

@Getter
public class PettyCashEvent extends ApplicationEvent {

    private Set<PettyCash> pettyCash;
    private boolean isEndorsed;
    private boolean isApproved;

    public PettyCashEvent(Object source, Set<PettyCash> pettyCash) {
        super(source);
        this.pettyCash = pettyCash;
        this.isEndorsed = pettyCash.stream().findFirst().get().getEndorsement().equals(EndorsementStatus.ENDORSED);
        this.isApproved = pettyCash.stream().findFirst().get().getApproval().equals(RequestApproval.APPROVED);
    }

    @Override
    public String toString() {
        return "PettyCashEvent{" +
                "pettyCash=" + pettyCash +
                '}';
    }
}
