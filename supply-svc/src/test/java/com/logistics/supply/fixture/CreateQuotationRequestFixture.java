package com.logistics.supply.fixture;


import com.logistics.supply.dto.CreateQuotationRequest;

import java.util.Set;

public class CreateQuotationRequestFixture {

    CreateQuotationRequestFixture() {
    }

    public static CreateQuotationRequest getCreateQuotationRequest() {

        Set<Integer> requestItemIds = Set.of(100);
        CreateQuotationRequest createQuotationRequest = new CreateQuotationRequest();
        createQuotationRequest.setSupplierId(2);
        createQuotationRequest.setDocumentId(100);
        createQuotationRequest.setRequestItemIds(requestItemIds);
        return createQuotationRequest;
    }
}
