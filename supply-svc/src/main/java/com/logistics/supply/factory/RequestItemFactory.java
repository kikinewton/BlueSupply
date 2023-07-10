package com.logistics.supply.factory;

import com.logistics.supply.dto.LpoMinorRequestItem;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.util.IdentifierUtil;

import java.util.concurrent.atomic.AtomicLong;

public class RequestItemFactory {

     RequestItemFactory() {
    }

    public static RequestItem getRequestItem(
            Employee employee,
            AtomicLong refCount,
            LpoMinorRequestItem lpoMinorRequestItem) {
        RequestItem requestItem = new RequestItem();
        requestItem.setReason(lpoMinorRequestItem.getReason());
        requestItem.setName(lpoMinorRequestItem.getName());
        requestItem.setPurpose(lpoMinorRequestItem.getPurpose());
        requestItem.setQuantity(lpoMinorRequestItem.getQuantity());
        requestItem.setRequestType(lpoMinorRequestItem.getRequestType());
        requestItem.setUserDepartment(lpoMinorRequestItem.getUserDepartment());
        requestItem.setPriorityLevel(lpoMinorRequestItem.getPriorityLevel());
        String ref =
                IdentifierUtil.idHandler(
                        "RQI",
                        employee.getDepartment().getName(),
                        String.valueOf(refCount.get()));
        requestItem.setRequestItemRef(ref);
        refCount.incrementAndGet();
        requestItem.setEmployee(employee);
        requestItem.setReceivingStore(lpoMinorRequestItem.getReceivingStore());
        return requestItem;
    }
}
