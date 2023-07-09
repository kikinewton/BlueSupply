package com.logistics.supply.factory;

import com.logistics.supply.dto.LpoMinorRequestItem;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.util.IdentifierUtil;

import java.util.concurrent.atomic.AtomicLong;

public class RequestItemFactory {

     RequestItemFactory() {
    }

    public static RequestItem getRequestItem(Employee employee, AtomicLong refCount, LpoMinorRequestItem r) {
        RequestItem requestItem = new RequestItem();
        requestItem.setReason(r.getReason());
        requestItem.setName(r.getName());
        requestItem.setPurpose(r.getPurpose());
        requestItem.setQuantity(r.getQuantity());
        requestItem.setRequestType(r.getRequestType());
        requestItem.setUserDepartment(r.getUserDepartment());
        requestItem.setPriorityLevel(r.getPriorityLevel());
        String ref =
                IdentifierUtil.idHandler(
                        "RQI",
                        employee.getDepartment().getName(),
                        String.valueOf(refCount.get()));
        requestItem.setRequestItemRef(ref);
        refCount.incrementAndGet();
        requestItem.setEmployee(employee);
        return requestItem;
    }
}
