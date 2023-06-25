package com.logistics.supply.fixture;

import com.logistics.supply.model.Supplier;

public class SupplierFixture {

    SupplierFixture() {
    }

    public static Supplier getSupplier(String name) {
        Supplier supplier = new Supplier();
        supplier.setId(1);
        supplier.setName(name);
        return supplier;
    }
}
