package com.logistics.supply.fixture;

import com.logistics.supply.dto.ReqItems;
import com.logistics.supply.enums.PriorityLevel;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.enums.RequestType;

public class RequestItemFixture {

    RequestItemFixture() {
    }

    public static ReqItems getReqItems() {
        return ReqItems.builder()
                .name("")
                .priorityLevel(PriorityLevel.NORMAL)
                .purpose("Special usage")
                .quantity(2)
                .requestType(RequestType.GOODS_REQUEST)
                .reason(RequestReason.FreshNeed)
                .build();
    }
}
