package com.logistics.supply.fixture;

import com.logistics.supply.model.RequestItem;

public class RequestItemFixture {

     RequestItemFixture() {
    }

    public static RequestItem getRequestItem(int id) {
        RequestItem requestItem = new RequestItem();
        requestItem.setId(id);
        return requestItem;
    }

}
