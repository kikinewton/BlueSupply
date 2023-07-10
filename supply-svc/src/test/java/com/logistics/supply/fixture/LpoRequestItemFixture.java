package com.logistics.supply.fixture;

import com.logistics.supply.dto.LpoMinorRequestItem;
import com.logistics.supply.enums.PriorityLevel;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.enums.RequestType;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Store;

public class LpoRequestItemFixture {

    LpoRequestItemFixture() {
    }

    public static LpoMinorRequestItem getGoodsRequestItem(
            String name,
            int quantity,
            Department department) {

        Store store = new Store();
        store.setId(100);

        return LpoMinorRequestItem.builder()
                .name(name)
                .priorityLevel(PriorityLevel.NORMAL)
                .purpose("Special usage")
                .quantity(quantity)
                .userDepartment(department)
                .receivingStore(store)
                .requestType(RequestType.GOODS_REQUEST)
                .reason(RequestReason.FreshNeed)
                .build();
    }

    public static LpoMinorRequestItem getServiceRequestItem(
            String name,
            Department department) {
        return LpoMinorRequestItem.builder()
                .name(name)
                .priorityLevel(PriorityLevel.NORMAL)
                .purpose("Special usage")
                .quantity(1)
                .userDepartment(department)
                .requestType(RequestType.SERVICE_REQUEST)
                .reason(RequestReason.FreshNeed)
                .build();
    }
}
