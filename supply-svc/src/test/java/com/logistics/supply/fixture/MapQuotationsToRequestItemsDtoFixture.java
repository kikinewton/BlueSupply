package com.logistics.supply.fixture;

import com.logistics.supply.dto.MapQuotationsToRequestItemsDto;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestItem;

import java.util.List;
import java.util.Set;

public class MapQuotationsToRequestItemsDtoFixture {

    public static MapQuotationsToRequestItemsDto getMapQuotationsToRequestItemsDto() {

        RequestItem requestItem = RequestItemFixture.getRequestItem(101);
        Set<RequestItem> requestItems = Set.of(requestItem);

        Quotation quotation = QuotationFixture.getQuotation(100);
        List<Quotation> quotations = List.of(quotation);
        return new MapQuotationsToRequestItemsDto(requestItems, quotations);
    }
}
