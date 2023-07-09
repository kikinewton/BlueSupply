package com.logistics.supply.fixture;

import com.logistics.supply.dto.MappingSuppliersAndRequestItemsDto;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;

import java.util.Set;

public class MappingSuppliersAndRequestItemsDtoFixture {

     MappingSuppliersAndRequestItemsDtoFixture() {
    }

    public static MappingSuppliersAndRequestItemsDto getSuppliersAndRequestItemsDto() {
        RequestItem requestItem = RequestItemFixture.getRequestItem(101);
        Set<RequestItem> requestItems = Set.of(requestItem);

        Supplier supplier = SupplierFixture.getSupplier("Jilorm Ventures");
        Set<Supplier> suppliers = Set.of(supplier);
        return new MappingSuppliersAndRequestItemsDto( suppliers, requestItems);
    }
}
