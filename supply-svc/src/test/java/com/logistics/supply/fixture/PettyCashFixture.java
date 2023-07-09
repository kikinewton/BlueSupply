package com.logistics.supply.fixture;

import com.logistics.supply.model.PettyCash;

import java.math.BigDecimal;

public class PettyCashFixture {

    PettyCashFixture() {
    }

    public static PettyCash getPettyCash(int pettyCashId) {
        PettyCash pettyCash = new PettyCash();
        pettyCash.setId(pettyCashId);
        pettyCash.setName("Gasoline");
        pettyCash.setAmount(BigDecimal.TEN);
        pettyCash.setQuantity(1);
        pettyCash.setStaffId("P4993");
        return pettyCash;
    }
}
